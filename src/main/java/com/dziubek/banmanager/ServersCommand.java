package com.dziubek.banmanager;

import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.plugin.Command;

/** /servers - lista serwerów z proxy, liczbą graczy i statusem zamknięcia (/shutdown). */
public class ServersCommand extends Command {

    private static final String PERMISSION = "banmanager.servers";

    private final BanManagerPlugin plugin;

    public ServersCommand(BanManagerPlugin plugin) {
        super("servers", null, "serwery");
        this.plugin = plugin;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (!Permissions.has(sender, PERMISSION)) {
            sender.sendMessage(TextComponent.fromLegacyText("§cNie masz uprawnień do tej komendy."));
            return;
        }

        sender.sendMessage(TextComponent.fromLegacyText("§8§m----§r §e§lServery §8§m----"));
        for (ServerInfo info : ProxyServer.getInstance().getServers().values()) {
            boolean closed = plugin.getMaintenance().isClosed(info.getName());
            String status = closed ? "§c§lZAMKNIĘTY" : "§a" + info.getPlayers().size() + " online";
            sender.sendMessage(TextComponent.fromLegacyText("§7- §f" + info.getName() + " §7- " + status));
        }
    }
}
