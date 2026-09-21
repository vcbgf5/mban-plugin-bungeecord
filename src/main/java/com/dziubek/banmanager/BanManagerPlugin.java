package com.dziubek.banmanager;

import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;

public class BanManagerPlugin extends Plugin {

    private BanStorage storage;
    private String fallbackServer = "";

    @Override
    public void onEnable() {
        Configuration config = loadOrCreateConfig();
        fallbackServer = config.getString("fallback-server", "");

        storage = new BanStorage(this);
        storage.load();

        getProxy().getPluginManager().registerCommand(this, new BanCommand(this));
        getProxy().getPluginManager().registerCommand(this, new BanIpCommand(this));
        getProxy().getPluginManager().registerCommand(this, new UnbanCommand(this));
        getProxy().getPluginManager().registerCommand(this, new UnbanIpCommand(this));
        getProxy().getPluginManager().registerCommand(this, new BanListCommand(this));

        getProxy().getPluginManager().registerListener(this, new BanListener(this));

        getLogger().info("BanManager wlaczony - aktywnych banow: " + storage.count());
    }

    public BanStorage getStorage() {
        return storage;
    }

    public String getFallbackServer() {
        return fallbackServer;
    }

    private Configuration loadOrCreateConfig() {
        if (!getDataFolder().exists()) {
            getDataFolder().mkdirs();
        }
        File file = new File(getDataFolder(), "config.yml");
        if (!file.exists()) {
            try (InputStream in = getResourceAsStream("config.yml")) {
                if (in != null) {
                    Files.copy(in, file.toPath());
                }
            } catch (IOException e) {
                getLogger().warning("Nie udalo sie utworzyc config.yml: " + e.getMessage());
            }
        }
        try {
            return ConfigurationProvider.getProvider(YamlConfiguration.class).load(file);
        } catch (IOException e) {
            getLogger().warning("Nie udalo sie wczytac config.yml: " + e.getMessage());
            return new Configuration();
        }
    }
}
