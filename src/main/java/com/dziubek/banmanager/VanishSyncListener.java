package com.dziubek.banmanager;

import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.ServerSwitchEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

/**
 * Gdy vanished admin przechodzi na inny serwer, BackendManager na nowym serwerze nic o nim
 * jeszcze nie wie (stan vanish trzymany jest tylko po stronie proxy) - trzeba więc po każdym
 * przełączeniu serwera ponownie wysłać VANISH_ON, żeby ukrycie "podążało" za graczem.
 */
public class VanishSyncListener implements Listener {

    private final BanManagerPlugin plugin;

    public VanishSyncListener(BanManagerPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onServerSwitch(ServerSwitchEvent event) {
        ProxiedPlayer player = event.getPlayer();
        if (plugin.getVanish().isVanished(player.getUniqueId())) {
            ControlChannel.sendVanish(player, true);
        }
    }
}
