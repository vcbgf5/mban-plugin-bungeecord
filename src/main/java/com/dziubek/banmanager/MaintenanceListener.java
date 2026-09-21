package com.dziubek.banmanager;

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

        String text = "§4§lSERWER ZAMKNIĘTY\n§7" + (plugin.getMaintenance().isAllClosed()
                ? "Cały proxy jest obecnie zamknięty (prace techniczne)." : "Serwer '" + server + "' jest obecnie zamknięty (prace techniczne).");

        ServerInfo alt = MenuHelper.findOpenAlternative(server, plugin);
        if (alt != null) {
            player.sendMessage(TextComponent.fromLegacyText(text));
            event.setTarget(alt);
            return;
        }

        event.setCancelled(true);
        if (player.getServer() != null) {
            player.sendMessage(TextComponent.fromLegacyText(text));
        } else {
            player.disconnect(TextComponent.fromLegacyText(text));
        }
    }
}
