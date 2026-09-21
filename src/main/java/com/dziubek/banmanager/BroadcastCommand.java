package com.dziubek.banmanager;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

/** /broadcast <wiadomość> - ogłoszenie do wszystkich graczy na całym proxy (wszystkie serwery). */
public class BroadcastCommand extends Command {

    private static final String PERMISSION = "banmanager.broadcast";

    public BroadcastCommand() {
        super("broadcast", null, "bc");
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (!Permissions.has(sender, PERMISSION)) {
            sender.sendMessage(TextComponent.fromLegacyText("§cNie masz uprawnień do tej komendy."));
            return;
        }
        if (args.length == 0) {
            sender.sendMessage(TextComponent.fromLegacyText("§cUżycie: /broadcast <wiadomość>"));
            return;
        }
        String message = ChatColor.translateAlternateColorCodes('&', String.join(" ", args));
        String full = "§8[§d§lOGŁOSZENIE§8] §f" + message;
        for (ProxiedPlayer player : ProxyServer.getInstance().getPlayers()) {
            player.sendMessage(TextComponent.fromLegacyText(full));
        }
        ProxyServer.getInstance().getConsole().sendMessage(TextComponent.fromLegacyText(full));
    }
}
