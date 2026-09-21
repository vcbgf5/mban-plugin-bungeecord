package com.dziubek.banmanager;

import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

/** /online (alias /list) - gracze online pogrupowani po serwerach, pomija ukrytych przez /vanish. */
public class OnlineCommand extends Command {

    private static final String PERMISSION = "banmanager.online";

    private final BanManagerPlugin plugin;

    public OnlineCommand(BanManagerPlugin plugin) {
        super("online", null, "list");
        this.plugin = plugin;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (!Permissions.has(sender, PERMISSION)) {
            sender.sendMessage(TextComponent.fromLegacyText("§cNie masz uprawnień do tej komendy."));
            return;
        }
        sender.sendMessage(TextComponent.fromLegacyText("§8§m----§r §e§lOnline §8§m---- §7(" + visibleCount() + ")"));
        for (ServerInfo info : ProxyServer.getInstance().getServers().values()) {
            StringBuilder names = new StringBuilder();
            int count = 0;
            for (ProxiedPlayer player : info.getPlayers()) {
                if (plugin.getVanish().isVanished(player.getUniqueId())) {
                    continue;
                }
                if (count > 0) {
                    names.append("§7, ");
                }
                names.append("§f").append(player.getName());
                count++;
            }
            sender.sendMessage(TextComponent.fromLegacyText("§b" + info.getName() + " §7(" + count + "): " + names));
        }
    }

    private int visibleCount() {
        int total = 0;
        for (ProxiedPlayer player : ProxyServer.getInstance().getPlayers()) {
            if (!plugin.getVanish().isVanished(player.getUniqueId())) {
                total++;
            }
        }
        return total;
    }
}
