package com.dziubek.banmanager;

import net.md_5.bungee.config.Configuration;

import java.util.LinkedHashMap;
import java.util.Map;

/** Wczytuje sekcję "motd" z config.yml - patrz komentarz w pliku dla znaczenia pól. */
public class MotdManager {

    private boolean enabled = true;
    private String networkName = "VantaNet";
    private final Map<String, String> servers = new LinkedHashMap<>();

    public void load(Configuration config) {
        Configuration motd = config.getSection("motd");
        enabled = motd.getBoolean("enabled", true);
        networkName = motd.getString("network-name", "VantaNet");

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

    /** Nazwa serwera (jak w config.yml Bungee/Waterfall) -> ładna nazwa do wyświetlenia. */
    public Map<String, String> getServers() {
        return servers;
    }
}
