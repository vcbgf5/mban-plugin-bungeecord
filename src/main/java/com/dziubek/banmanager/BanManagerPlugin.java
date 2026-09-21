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
    private MuteManager mutes;
    private MaintenanceManager maintenance;
    private VanishManager vanish;
    private WarnManager warnings;
    private String fallbackServer = "";

    @Override
    public void onEnable() {
        Configuration config = loadOrCreateConfig();
        fallbackServer = config.getString("fallback-server", "");

        storage = new BanStorage(this);
        storage.load();
        mutes = new MuteManager(this);
        mutes.load();
        maintenance = new MaintenanceManager();
        vanish = new VanishManager();
        warnings = new WarnManager();

        getProxy().getPluginManager().registerCommand(this, new BanCommand(this));
        getProxy().getPluginManager().registerCommand(this, new BanIpCommand(this));
        getProxy().getPluginManager().registerCommand(this, new UnbanCommand(this));
        getProxy().getPluginManager().registerCommand(this, new UnbanIpCommand(this));
        getProxy().getPluginManager().registerCommand(this, new BanListCommand(this));

        ShutdownCommand shutdownCommand = new ShutdownCommand(this);
        getProxy().getPluginManager().registerCommand(this, shutdownCommand);
        getProxy().getPluginManager().registerCommand(this, new StartServerCommand(shutdownCommand));

        getProxy().getPluginManager().registerCommand(this, new BroadcastCommand());
        getProxy().getPluginManager().registerCommand(this, new AlertCommand());
        getProxy().getPluginManager().registerCommand(this, new FindCommand());
        getProxy().getPluginManager().registerCommand(this, new SendCommand());
        getProxy().getPluginManager().registerCommand(this, new ServersCommand(this));
        getProxy().getPluginManager().registerCommand(this, new KickCommand());
        getProxy().getPluginManager().registerCommand(this, new WhoisCommand(this));
        getProxy().getPluginManager().registerCommand(this, new OnlineCommand(this));
        getProxy().getPluginManager().registerCommand(this, new VanishCommand(this));
        getProxy().getPluginManager().registerCommand(this, new WarnCommand(this));
        getProxy().getPluginManager().registerCommand(this, new WarningsCommand(this));
        getProxy().getPluginManager().registerCommand(this, new ClearChatCommand());
        getProxy().getPluginManager().registerCommand(this, new ReloadCommand(this));
        getProxy().getPluginManager().registerCommand(this, new MuteCommand(this));
        getProxy().getPluginManager().registerCommand(this, new UnmuteCommand(this));
        getProxy().getPluginManager().registerCommand(this, new MuteListCommand(this));

        getProxy().getPluginManager().registerListener(this, new BanListener(this));
        getProxy().getPluginManager().registerListener(this, new MaintenanceListener(this));
        getProxy().getPluginManager().registerListener(this, new MuteListener(this));

        getProxy().registerChannel("banmanager:query");
        getProxy().getPluginManager().registerListener(this, new BanQueryListener(this));

        getLogger().info("BanManager wlaczony - aktywnych banow: " + storage.count() + ", wyciszen: " + mutes.list(null).size());
    }

    /** /bmreload - configu.yml (fallback-server) i danych z dysku (na wypadek recznej edycji plikow). */
    public void reload() {
        Configuration config = loadOrCreateConfig();
        fallbackServer = config.getString("fallback-server", "");
        storage.load();
        mutes.load();
    }

    public BanStorage getStorage() {
        return storage;
    }

    public MuteManager getMutes() {
        return mutes;
    }

    public MaintenanceManager getMaintenance() {
        return maintenance;
    }

    public VanishManager getVanish() {
        return vanish;
    }

    public WarnManager getWarnings() {
        return warnings;
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
