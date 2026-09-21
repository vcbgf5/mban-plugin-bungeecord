package com.dziubek.banmanager;

import net.md_5.bungee.api.ChatColor;
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

/** /alert <serwer> <wiadomość> - ogłoszenie tylko dla graczy na JEDNYM serwerze (w odróżnieniu od /broadcast). */
public class AlertCommand extends Command implements TabExecutor {

    private static final String PERMISSION = "banmanager.broadcast";

    public AlertCommand() {
        super("alert", null);
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (!Permissions.has(sender, PERMISSION)) {
            sender.sendMessage(TextComponent.fromLegacyText("§cNie masz uprawnień do tej komendy."));
            return;
        }
        if (args.length < 2) {
            sender.sendMessage(TextComponent.fromLegacyText("§cUżycie: /alert <serwer> <wiadomość>"));
            return;
        }
        ServerInfo info = ProxyServer.getInstance().getServerInfo(args[0]);
        if (info == null) {
            sender.sendMessage(TextComponent.fromLegacyText("§cNieznany serwer '" + args[0] + "'."));
            return;
        }
        String message = ChatColor.translateAlternateColorCodes('&', String.join(" ", Arrays.copyOfRange(args, 1, args.length)));
        String full = "§8[§b§l" + info.getName() + "§8] §f" + message;
        for (ProxiedPlayer player : info.getPlayers()) {
            player.sendMessage(TextComponent.fromLegacyText(full));
        }
        sender.sendMessage(TextComponent.fromLegacyText("§aWysłano do §f" + info.getPlayers().size() + " §agraczy na §f" + info.getName() + "§a."));
    }

    @Override
    public Iterable<String> onTabComplete(CommandSender sender, String[] args) {
        List<String> options = new ArrayList<>();
        if (args.length <= 1) {
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
