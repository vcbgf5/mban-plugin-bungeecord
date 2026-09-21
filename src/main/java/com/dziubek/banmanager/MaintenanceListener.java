package com.dziubek.banmanager;

import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.ServerConnectEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

/**
 * Egzekwuje zamknięcia z /shutdown - blokuje/przekierowuje próby wejścia na zamknięty serwer
 * (albo caly proxy). Admini z uprawnieniem banmanager.shutdown.bypass wchodzą normalnie (żeby
 * móc coś naprawić w trakcie konserwacji).
 */
public class MaintenanceListener implements Listener {

    private static final String BYPASS_PERMISSION = "banmanager.shutdown.bypass";

    private final BanManagerPlugin plugin;

    public MaintenanceListener(BanManagerPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onServerConnect(ServerConnectEvent event) {
        ProxiedPlayer player = event.getPlayer();
        if (Permissions.has(player, BYPASS_PERMISSION)) {
            return;
        }

        String server = event.getTarget().getName();
        if (!plugin.getMaintenance().isClosed(server)) {
            return;
        }

        ServerInfo alt = findOpenAlternative(server);
        if (alt != null) {
            event.setTarget(alt);
            return;
        }

        event.setCancelled(true);
        String text = "§4§lSERWER ZAMKNIĘTY\n§7" + (plugin.getMaintenance().isAllClosed()
                ? "Cały proxy jest obecnie zamknięty." : "Serwer '" + server + "' jest obecnie zamknięty.");
        if (player.getServer() != null) {
            player.sendMessage(TextComponent.fromLegacyText(text));
        } else {
            player.disconnect(TextComponent.fromLegacyText(text));
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
}
