package com.dziubek.banmanager;

import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.TabExecutor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * /mban <gracz> <global|serwer> <czas> <powod> - bez koncowych argumentow pokazuje klikalne
 * menu (zasieg -> czas -> powod) zamiast wymuszac wpisanie wszystkiego od razu.
 */
public class BanCommand extends Command implements TabExecutor {

    private final BanManagerPlugin plugin;

    public BanCommand(BanManagerPlugin plugin) {
        super("mban", "banmanager.ban");
        this.plugin = plugin;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (args.length == 0) {
            sender.sendMessage(TextComponent.fromLegacyText("§cUzycie: /mban <gracz> <global|serwer> <czas> <powod>"));
            return;
        }
        String targetName = args[0];

        if (args.length == 1) {
            if (!(sender instanceof ProxiedPlayer)) {
                sender.sendMessage(TextComponent.fromLegacyText("§cPodaj zasieg: /mban " + targetName + " <global|serwer> <czas> <powod>"));
                return;
            }
            MenuHelper.sendScopeMenu((ProxiedPlayer) sender, "mban", targetName);
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
                sender.sendMessage(TextComponent.fromLegacyText("§cPodaj czas: /mban " + targetName + " " + args[1] + " <czas> <powod>"));
                return;
            }
            MenuHelper.sendDurationMenu((ProxiedPlayer) sender, "mban", targetName, args[1]);
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
                sender.sendMessage(TextComponent.fromLegacyText("§cPodaj powod: /mban " + targetName + " " + args[1] + " " + args[2] + " <powod>"));
                return;
            }
            MenuHelper.sendReasonMenu((ProxiedPlayer) sender, "mban", targetName, args[1], args[2]);
            return;
        }

        String reason = String.join(" ", Arrays.copyOfRange(args, 3, args.length));
        String by = sender instanceof ProxiedPlayer ? sender.getName() : "Console";

        ProxiedPlayer online = ProxyServer.getInstance().getPlayer(targetName);
        String lookupName = online != null ? online.getName() : targetName;

        BanEntry entry = plugin.getStorage().addBan(BanEntry.Type.PLAYER, lookupName.toLowerCase(), lookupName, scope, reason, by, durationMillis);
        sender.sendMessage(TextComponent.fromLegacyText("§aZbanowano §f" + lookupName + " §7[" + scope + "] §7na §f"
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
