package com.dziubek.banmanager;

import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.ServerPing;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.event.ProxyPingEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Własne MOTD: krótki, mały napis (nazwa sieci + BOXPVP), bez liczby graczy w samym opisie -
 * rozbicie graczy per-serwer + suma widać dopiero po najechaniu na licznik graczy (podpięte
 * pod "sample" listy graczy - Minecraft renderuje ją jako tooltip pod liczbą online/max).
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

        String title = GradientText.apply(motd.getNetworkName(), GRADIENT_FROM, GRADIENT_TO, false);
        String subtitle = "§7BOXPVP";

        event.getResponse().setDescriptionComponent(
                new TextComponent(TextComponent.fromLegacyText(title + "\n" + subtitle))
        );

        List<ServerPing.PlayerInfo> sample = new ArrayList<>();
        int total = 0;
        for (Map.Entry<String, String> entry : motd.getServers().entrySet()) {
            ServerInfo info = ProxyServer.getInstance().getServerInfo(entry.getKey());
            int online = info != null ? info.getPlayers().size() : 0;
            total += online;
            sample.add(new ServerPing.PlayerInfo("§7" + entry.getValue() + "§8: §f" + online, UUID.randomUUID()));
        }
        sample.add(new ServerPing.PlayerInfo("§7Łącznie§8: §f" + total, UUID.randomUUID()));

        ServerPing.Players players = event.getResponse().getPlayers();
        if (players != null) {
            players.setSample(sample.toArray(new ServerPing.PlayerInfo[0]));
        }
    }
}
