package com.dziubek.banmanager;

import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.TabExecutor;

import java.util.ArrayList;
import java.util.List;

/** /send <gracz|all> <serwer> - wymusza teleportację gracza (albo wszystkich) na dany serwer. */
public class SendCommand extends Command implements TabExecutor {

    private static final String PERMISSION = "banmanager.send";

    public SendCommand() {
        super("send", null);
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (!Permissions.has(sender, PERMISSION)) {
            sender.sendMessage(TextComponent.fromLegacyText("§cNie masz uprawnień do tej komendy."));
            return;
        }
        if (args.length < 2) {
            sender.sendMessage(TextComponent.fromLegacyText("§cUżycie: /send <gracz|all> <serwer>"));
            return;
        }
        ServerInfo target = ProxyServer.getInstance().getServerInfo(args[1]);
        if (target == null) {
            sender.sendMessage(TextComponent.fromLegacyText("§cNieznany serwer '" + args[1] + "'."));
            return;
        }

        if (args[0].equalsIgnoreCase("all")) {
            int count = 0;
            for (ProxiedPlayer player : ProxyServer.getInstance().getPlayers()) {
                player.connect(target);
                count++;
            }
            sender.sendMessage(TextComponent.fromLegacyText("§aWysłano " + count + " graczy na §f" + target.getName() + "§a."));
            return;
        }

        ProxiedPlayer player = ProxyServer.getInstance().getPlayer(args[0]);
        if (player == null) {
            sender.sendMessage(TextComponent.fromLegacyText("§cGracz '" + args[0] + "' nie jest online."));
            return;
        }
        player.connect(target);
        sender.sendMessage(TextComponent.fromLegacyText("§aWysłano §f" + player.getName() + " §ana §f" + target.getName() + "§a."));
    }

    @Override
    public Iterable<String> onTabComplete(CommandSender sender, String[] args) {
        List<String> options = new ArrayList<>();
        if (args.length <= 1) {
            options.add("all");
            for (ProxiedPlayer player : ProxyServer.getInstance().getPlayers()) {
                options.add(player.getName());
            }
        } else if (args.length == 2) {
            options.addAll(ProxyServer.getInstance().getServers().keySet());
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
