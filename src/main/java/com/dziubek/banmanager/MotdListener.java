package com.dziubek.banmanager;

import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.event.ProxyPingEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

import java.util.Map;

/**
 * Własne MOTD: gradientowa nazwa sieci w 1. linii, w 2. linii (mniejsza/szara, "cichsza" niż
 * pogrubiony tytuł) liczba graczy na każdym skonfigurowanym serwerze proxy + suma łączna.
 */
public class MotdListener implements Listener {

    private static final int GRADIENT_FROM = 0x4B0082; // indigo / ciemny fiolet
    private static final int GRADIENT_TO = 0xD8B4FE;   // jasny fiolet / lawenda

    private final BanManagerPlugin plugin;

    public MotdListener(BanManagerPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPing(ProxyPingEvent event) {
        MotdManager motd = plugin.getMotd();
        if (!motd.isEnabled()) {
            return;
        }

        String title = GradientText.apply(motd.getNetworkName(), GRADIENT_FROM, GRADIENT_TO, true);

        StringBuilder statsLine = new StringBuilder("§7");
        int total = 0;
        boolean first = true;
        for (Map.Entry<String, String> entry : motd.getServers().entrySet()) {
            ServerInfo info = ProxyServer.getInstance().getServerInfo(entry.getKey());
            int online = info != null ? info.getPlayers().size() : 0;
            total += online;

            if (!first) {
                statsLine.append(" §8| §7");
            }
            statsLine.append(entry.getValue()).append(" §f").append(online);
            first = false;
        }
        if (!first) {
            statsLine.append(" §8| §7Łącznie §f").append(total);
        } else {
            statsLine.append("Online łącznie: §f").append(total);
        }

        event.getResponse().setDescriptionComponent(
                TextComponent.fromLegacyText(title + "\n" + statsLine)
        );
    }
}
