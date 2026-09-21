package com.dziubek.banmanager;

import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.TabExecutor;
import net.md_5.bungee.api.scheduler.ScheduledTask;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * /shutdown <serwer|all> <sekundy> [powod] - odlicza na czacie GRACZOM NA TYM SERWERZE (albo
 * wszystkim, jeśli "all"), a po odliczeniu przenosi ich na inny wolny serwer (jeśli jakiś jest)
 * albo rozłącza. Serwer zostaje potem oznaczony jako "zamknięty" - nikt nowy nie wejdzie, dopóki
 * admin nie zrobi /shutdown cancel. To NIE zabija procesu Javy backendu (proxy tego nie potrafi) -
 * tylko blokuje/opróżnia serwer na poziomie proxy; sam restart/wyłączenie procesu robi się osobno.
 */
public class ShutdownCommand extends Command implements TabExecutor {

    private static final int[] WARN_THRESHOLDS = {300, 120, 60, 30, 10, 5, 4, 3, 2, 1};

    private final BanManagerPlugin plugin;
    private final Map<String, ScheduledTask> activeCountdowns = new ConcurrentHashMap<>();

    private static final String PERMISSION = "banmanager.shutdown";

    public ShutdownCommand(BanManagerPlugin plugin) {
        super("shutdown", null);
        this.plugin = plugin;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (!Permissions.has(sender, PERMISSION)) {
            sender.sendMessage(TextComponent.fromLegacyText("§cNie masz uprawnień do tej komendy."));
            return;
        }
        if (args.length == 0) {
            sender.sendMessage(TextComponent.fromLegacyText(
                    "§cUzycie: /shutdown <serwer|all> <sekundy> [powod] | /shutdown cancel <serwer|all> | /shutdown list"));
            return;
        }

        if (args[0].equalsIgnoreCase("list")) {
            handleList(sender);
            return;
        }
        if (args[0].equalsIgnoreCase("cancel")) {
            if (args.length < 2) {
                sender.sendMessage(TextComponent.fromLegacyText("§cUzycie: /shutdown cancel <serwer|all>"));
                return;
            }
            handleCancel(sender, args[1]);
            return;
        }

        String scope = args[0].equalsIgnoreCase("all") ? "ALL" : args[0];
        if (!scope.equals("ALL") && ProxyServer.getInstance().getServerInfo(scope) == null) {
            sender.sendMessage(TextComponent.fromLegacyText("§cNieznany serwer '" + args[0] + "'."));
            return;
        }
        if (args.length < 2) {
            sender.sendMessage(TextComponent.fromLegacyText("§cPodaj czas w sekundach: /shutdown " + args[0] + " <sekundy> [powod]"));
            return;
        }

        int seconds;
        try {
            seconds = Integer.parseInt(args[1]);
        } catch (NumberFormatException e) {
            sender.sendMessage(TextComponent.fromLegacyText("§cCzas musi być liczbą sekund."));
            return;
        }
        if (seconds < 1) {
            sender.sendMessage(TextComponent.fromLegacyText("§cCzas musi być większy od 0."));
            return;
        }
        if (activeCountdowns.containsKey(scope)) {
            sender.sendMessage(TextComponent.fromLegacyText("§cOdliczanie dla '" + args[0] + "' już trwa - najpierw /shutdown cancel " + args[0] + "."));
            return;
        }

        String reason = args.length > 2 ? String.join(" ", Arrays.copyOfRange(args, 2, args.length)) : "Konserwacja";
        String by = sender instanceof ProxiedPlayer ? sender.getName() : "Console";

        sender.sendMessage(TextComponent.fromLegacyText("§aZaplanowano zamknięcie " + describeScope(scope) + " za " + seconds + "s. Powód: " + reason));
        startCountdown(scope, seconds, reason, by);
    }

    private void startCountdown(String scope, int totalSeconds, String reason, String by) {
        broadcastToScope(scope, "§c§l⚠ ZAMKNIĘCIE " + describeScope(scope).toUpperCase() + " ZA " + formatSeconds(totalSeconds) + " §7- " + reason);

        int[] remaining = {totalSeconds};
        ScheduledTask task = ProxyServer.getInstance().getScheduler().schedule(plugin, () -> {
            remaining[0]--;
            if (remaining[0] <= 0) {
                activeCountdowns.remove(scope);
                executeShutdown(scope, reason, by);
                return;
            }
            for (int threshold : WARN_THRESHOLDS) {
                if (remaining[0] == threshold) {
                    broadcastToScope(scope, "§c§l⚠ ZAMKNIĘCIE " + describeScope(scope).toUpperCase() + " ZA " + formatSeconds(remaining[0]) + " §7- " + reason);
                    break;
                }
            }
        }, 1, 1, TimeUnit.SECONDS);

        activeCountdowns.put(scope, task);
    }

    private void executeShutdown(String scope, String reason, String by) {
        if (scope.equals("ALL")) {
            plugin.getMaintenance().closeAll();
            for (ProxiedPlayer player : new ArrayList<>(ProxyServer.getInstance().getPlayers())) {
                player.disconnect(TextComponent.fromLegacyText("§4§lSERWER ZAMKNIĘTY\n§7Powód: §f" + reason
                        + "\n§7Przez: §f" + by + "\n§7Spróbuj ponownie za chwilę."));
            }
            return;
        }

        plugin.getMaintenance().close(scope);
        ServerInfo info = ProxyServer.getInstance().getServerInfo(scope);
        if (info == null) {
            return;
        }
        for (ProxiedPlayer player : new ArrayList<>(info.getPlayers())) {
            ServerInfo alt = findOpenAlternative(scope);
            if (alt != null) {
                player.sendMessage(TextComponent.fromLegacyText("§c" + scope + " został zamknięty (" + reason + ") - przenoszę Cię."));
                player.connect(alt);
            } else {
                player.disconnect(TextComponent.fromLegacyText("§4§lSERWER ZAMKNIĘTY\n§7Serwer: §f" + scope
                        + "\n§7Powód: §f" + reason + "\n§7Przez: §f" + by));
            }
        }
    }

    private ServerInfo findOpenAlternative(String excluded) {
        for (ServerInfo info : ProxyServer.getInstance().getServers().values()) {
            if (info.getName().equalsIgnoreCase(excluded)) {
                continue;
            }
            if (!plugin.getMaintenance().isClosed(info.getName())) {
                return info;
            }
        }
        return null;
    }

    private void handleCancel(CommandSender sender, String rawScope) {
        String scope = rawScope.equalsIgnoreCase("all") ? "ALL" : rawScope;
        ScheduledTask task = activeCountdowns.remove(scope);
        boolean hadCountdown = task != null;
        if (task != null) {
            task.cancel();
        }

        boolean wasClosed = scope.equals("ALL") ? plugin.getMaintenance().isAllClosed() : plugin.getMaintenance().isClosed(scope);
        if (scope.equals("ALL")) {
            plugin.getMaintenance().reopenAll();
        } else {
            plugin.getMaintenance().reopen(scope);
        }

        if (hadCountdown) {
            broadcastToScope(scope, "§aZamknięcie " + describeScope(scope) + " anulowane.");
        }
        sender.sendMessage(TextComponent.fromLegacyText(hadCountdown || wasClosed
                ? "§aAnulowano/otwarto " + describeScope(scope) + "."
                : "§eNie było aktywnego odliczenia ani zamknięcia dla " + describeScope(scope) + "."));
    }

    private void handleList(CommandSender sender) {
        sender.sendMessage(TextComponent.fromLegacyText("§8§m----§r §e§lStan zamknięć §8§m----"));
        boolean any = false;
        if (plugin.getMaintenance().isAllClosed()) {
            sender.sendMessage(TextComponent.fromLegacyText("§c§lCAŁY PROXY ZAMKNIĘTY"));
            any = true;
        }
        for (String server : plugin.getMaintenance().getClosedServers()) {
            sender.sendMessage(TextComponent.fromLegacyText("§c- " + server + " §7(zamknięty)"));
            any = true;
        }
        for (String scope : activeCountdowns.keySet()) {
            sender.sendMessage(TextComponent.fromLegacyText("§e- " + describeScope(scope) + " §7- odliczanie trwa"));
            any = true;
        }
        if (!any) {
            sender.sendMessage(TextComponent.fromLegacyText("§7Brak aktywnych zamknięć."));
        }
    }

    private void broadcastToScope(String scope, String legacyText) {
        if (scope.equals("ALL")) {
            for (ProxiedPlayer player : ProxyServer.getInstance().getPlayers()) {
                player.sendMessage(TextComponent.fromLegacyText(legacyText));
            }
            return;
        }
        ServerInfo info = ProxyServer.getInstance().getServerInfo(scope);
        if (info == null) {
            return;
        }
        for (ProxiedPlayer player : info.getPlayers()) {
            player.sendMessage(TextComponent.fromLegacyText(legacyText));
        }
    }

    private String describeScope(String scope) {
        return scope.equals("ALL") ? "cały proxy" : "serwer " + scope;
    }

    private String formatSeconds(int seconds) {
        if (seconds >= 60 && seconds % 60 == 0) {
            return (seconds / 60) + "min";
        }
        return seconds + "s";
    }

    @Override
    public Iterable<String> onTabComplete(CommandSender sender, String[] args) {
        List<String> options = new ArrayList<>();
        if (args.length <= 1) {
            options.add("all");
            options.add("cancel");
            options.add("list");
            options.addAll(ProxyServer.getInstance().getServers().keySet());
        } else if (args.length == 2) {
            if (args[0].equalsIgnoreCase("cancel")) {
                options.add("all");
                options.addAll(ProxyServer.getInstance().getServers().keySet());
            } else if (!args[0].equalsIgnoreCase("list")) {
                options.add("60");
                options.add("120");
                options.add("300");
            }
        }
        String current = args.length > 0 ? args[args.length - 1].toLowerCase() : "";
        List<String> filtered = new ArrayList<>();
        for (String option : options) {
            if (option.toLowerCase().startsWith(current)) {
                filtered.add(option);
            }
        }
        return filtered;
    }
}
