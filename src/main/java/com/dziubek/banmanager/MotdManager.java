package com.dziubek.banmanager;

import net.md_5.bungee.config.Configuration;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Wczytuje sekcję "motd" z config.yml - patrz komentarz w pliku dla znaczenia pól. */
public class MotdManager {

    private boolean enabled = true;
    private String networkName = "VantaNet";
    private int adRotateSeconds = 4;
    private final List<String> ads = new ArrayList<>();
    private final Map<String, String> servers = new LinkedHashMap<>();

    public void load(Configuration config) {
        Configuration motd = config.getSection("motd");
        enabled = motd.getBoolean("enabled", true);
        networkName = motd.getString("network-name", "VantaNet");
        adRotateSeconds = Math.max(1, motd.getInt("ad-rotate-seconds", 4));

        ads.clear();
        ads.addAll(motd.getStringList("ads"));

        servers.clear();
        Configuration serversSection = motd.getSection("servers");
        for (String key : serversSection.getKeys()) {
            servers.put(key, serversSection.getString(key, key));
        }
    }

    public boolean isEnabled() {
        return enabled;
    }

    public String getNetworkName() {
        return networkName;
    }

    public int getAdRotateSeconds() {
        return adRotateSeconds;
    }

    /** Kolejka reklamowych linijek pod nazwą sieci w MOTD - rotują się co ad-rotate-seconds. */
    public List<String> getAds() {
        return ads;
    }

    /** Nazwa serwera (jak w config.yml Bungee/Waterfall) -> ładna nazwa do wyświetlenia. */
    public Map<String, String> getServers() {
        return servers;
    }
}
