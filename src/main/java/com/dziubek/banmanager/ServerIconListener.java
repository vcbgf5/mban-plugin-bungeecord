package com.dziubek.banmanager;

import net.md_5.bungee.api.Favicon;
import net.md_5.bungee.api.event.ProxyPingEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

public class ServerIconListener implements Listener {

    private final BanManagerPlugin plugin;

    public ServerIconListener(BanManagerPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPing(ProxyPingEvent event) {
        Favicon favicon = plugin.getServerIcon().getFavicon();
        if (favicon != null) {
            event.getResponse().setFavicon(favicon);
        }
    }
}
