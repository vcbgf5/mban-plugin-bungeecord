package com.dziubek.banmanager;

import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.TabExecutor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * /mbanip <gracz|IP> <global|serwer> <czas> <powod> - jesli podasz nick gracza ktory jest
 * online, sam ustala jego aktualne IP (nie trzeba go znac na pamiec).
 */
public class BanIpCommand extends Command implements TabExecutor {

    private static final String PERMISSION = "banmanager.ban";

    private final BanManagerPlugin plugin;

    public BanIpCommand(BanManagerPlugin plugin) {
        super("mbanip", null);
        this.plugin = plugin;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (!Permissions.has(sender, PERMISSION)) {
            sender.sendMessage(TextComponent.fromLegacyText("§cNie masz uprawnień do tej komendy."));
            return;
        }
        if (args.length == 0) {
            sender.sendMessage(TextComponent.fromLegacyText("§cUzycie: /mbanip <gracz|IP> <global|serwer> <czas> <powod>"));
            return;
        }
        String targetArg = args[0];
        ProxiedPlayer online = ProxyServer.getInstance().getPlayer(targetArg);
        String ip = online != null ? MenuHelper.ipOf(online) : targetArg;
        if (ip == null) {
            sender.sendMessage(TextComponent.fromLegacyText("§cNie udalo sie ustalic adresu IP dla '" + targetArg + "'."));
            return;
        }
        String displayTarget = online != null ? online.getName() + " (" + ip + ")" : ip;

        if (args.length == 1) {
            if (!(sender instanceof ProxiedPlayer)) {
                sender.sendMessage(TextComponent.fromLegacyText("§cPodaj zasieg: /mbanip " + targetArg + " <global|serwer> <czas> <powod>"));
                return;
            }
            MenuHelper.sendScopeMenu((ProxiedPlayer) sender, "mbanip", targetArg);
            return;
        }

        String scope = args[1].equalsIgnoreCase("global") ? "GLOBAL" : args[1];
        if (!scope.equals("GLOBAL") && ProxyServer.getInstance().getServerInfo(scope) == null) {
            sender.sendMessage(TextComponent.fromLegacyText("§cNieznany serwer '" + args[1] + "'. Dostepne: global, "
                    + String.join(", ", ProxyServer.getInstance().getServers().keySet())));
            return;
        }

        if (args.length == 2) {
            if (!(sender instanceof ProxiedPlayer)) {
                sender.sendMessage(TextComponent.fromLegacyText("§cPodaj czas: /mbanip " + targetArg + " " + args[1] + " <czas> <powod>"));
                return;
            }
            MenuHelper.sendDurationMenu((ProxiedPlayer) sender, "mbanip", targetArg, args[1]);
            return;
        }

        long durationMillis;
        try {
            durationMillis = DurationParser.parse(args[2]);
        } catch (IllegalArgumentException e) {
            sender.sendMessage(TextComponent.fromLegacyText("§cZly format czasu '" + args[2] + "'. Przyklady: 30m, 1h, 7d, perm"));
            return;
        }

        if (args.length == 3) {
            if (!(sender instanceof ProxiedPlayer)) {
                sender.sendMessage(TextComponent.fromLegacyText("§cPodaj powod: /mbanip " + targetArg + " " + args[1] + " " + args[2] + " <powod>"));
                return;
            }
            MenuHelper.sendReasonMenu((ProxiedPlayer) sender, "mbanip", targetArg, args[1], args[2]);
            return;
        }

        String reason = String.join(" ", Arrays.copyOfRange(args, 3, args.length));
        String by = sender instanceof ProxiedPlayer ? sender.getName() : "Console";

        BanEntry entry = plugin.getStorage().addBan(BanEntry.Type.IP, ip, displayTarget, scope, reason, by, durationMillis);
        sender.sendMessage(TextComponent.fromLegacyText("§aZbanowano IP §f" + displayTarget + " §7[" + scope + "] §7na §f"
                + DurationParser.format(entry.getExpiresAt()) + " §7- §f" + reason));

        if (online == null) {
            return;
        }
        if (entry.isGlobal()) {
            online.disconnect(MenuHelper.banMessage(entry));
            return;
        }
        if (online.getServer() != null && online.getServer().getInfo().getName().equalsIgnoreCase(scope)) {
            ServerInfo alt = MenuHelper.findAlternative(scope, online, plugin);
            if (alt != null) {
                online.sendMessage(MenuHelper.banMessage(entry));
                online.connect(alt);
            } else {
                online.disconnect(MenuHelper.banMessage(entry));
            }
        }
    }

    @Override
    public Iterable<String> onTabComplete(CommandSender sender, String[] args) {
        List<String> options = new ArrayList<>();
        if (args.length <= 1) {
            for (ProxiedPlayer player : ProxyServer.getInstance().getPlayers()) {
                options.add(player.getName());
            }
        } else if (args.length == 2) {
            options.add("global");
            options.addAll(ProxyServer.getInstance().getServers().keySet());
        } else if (args.length == 3) {
            options.addAll(List.of("30m", "1h", "1d", "7d", "30d", "perm"));
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
