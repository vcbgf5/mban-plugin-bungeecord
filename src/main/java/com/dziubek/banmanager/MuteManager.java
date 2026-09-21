package com.dziubek.banmanager;

import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Wyciszenia na czacie - globalne (caly proxy naraz, bez podzialu na serwery - w odroznieniu od
 * bana, czat nie ma tu "zasiegu"). Trwale w mutes.yml. Egzekwowane przez MuteListener (ChatEvent -
 * proxy widzi tresc wiadomosci czatu, wiec moze je zablokowac zanim dojda do backendu).
 */
public class MuteManager {

    public static final class MuteEntry {
        private final String target;
        private final String display;
        private final String reason;
        private final String by;
        private final long expiresAt;

        MuteEntry(String target, String display, String reason, String by, long expiresAt) {
            this.target = target;
            this.display = display;
            this.reason = reason;
            this.by = by;
            this.expiresAt = expiresAt;
        }

        public String getTarget() {
            return target;
        }

        public String getDisplay() {
            return display;
        }

        public String getReason() {
            return reason;
        }

        public String getBy() {
            return by;
        }

        public long getExpiresAt() {
            return expiresAt;
        }

        public boolean isPermanent() {
            return expiresAt < 0;
        }

        public boolean isExpired() {
            return !isPermanent() && System.currentTimeMillis() >= expiresAt;
        }
    }

    private final BanManagerPlugin plugin;
    private final File file;
    private final List<MuteEntry> mutes = new ArrayList<>();

    public MuteManager(BanManagerPlugin plugin) {
        this.plugin = plugin;
        if (!plugin.getDataFolder().exists()) {
            plugin.getDataFolder().mkdirs();
        }
        this.file = new File(plugin.getDataFolder(), "mutes.yml");
    }

    public void load() {
        mutes.clear();
        try {
            if (!file.exists()) {
                file.createNewFile();
            }
            Configuration data = ConfigurationProvider.getProvider(YamlConfiguration.class).load(file);
            Configuration section = data.getSection("mutes");
            if (section != null) {
                for (String key : section.getKeys()) {
                    Configuration entry = section.getSection(key);
                    try {
                        mutes.add(new MuteEntry(
                                entry.getString("target"),
                                entry.getString("display", entry.getString("target")),
                                entry.getString("reason", "Brak podanego powodu"),
                                entry.getString("by", "Console"),
                                entry.getLong("expires-at", -1)
                        ));
                    } catch (Exception e) {
                        plugin.getLogger().warning("Uszkodzony wpis wyciszenia '" + key + "' w mutes.yml, pomijam.");
                    }
                }
            }
        } catch (IOException e) {
            plugin.getLogger().warning("Nie udalo sie wczytac mutes.yml: " + e.getMessage());
        }
    }

    private void save() {
        Configuration fresh = new Configuration();
        for (MuteEntry entry : mutes) {
            String base = "mutes." + entry.getTarget();
            fresh.set(base + ".target", entry.getTarget());
            fresh.set(base + ".display", entry.getDisplay());
            fresh.set(base + ".reason", entry.getReason());
            fresh.set(base + ".by", entry.getBy());
            fresh.set(base + ".expires-at", entry.getExpiresAt());
        }
        try {
            ConfigurationProvider.getProvider(YamlConfiguration.class).save(fresh, file);
        } catch (IOException e) {
            plugin.getLogger().warning("Nie udalo sie zapisac mutes.yml: " + e.getMessage());
        }
    }

    private boolean purgeExpired() {
        return mutes.removeIf(MuteEntry::isExpired);
    }

    public synchronized MuteEntry mute(String targetLower, String display, String reason, String by, long durationMillis) {
        purgeExpired();
        mutes.removeIf(m -> m.getTarget().equalsIgnoreCase(targetLower));
        long expiresAt = durationMillis < 0 ? -1 : System.currentTimeMillis() + durationMillis;
        MuteEntry entry = new MuteEntry(targetLower, display, reason, by, expiresAt);
        mutes.add(entry);
        save();
        return entry;
    }

    public synchronized boolean unmute(String targetLower) {
        purgeExpired();
        boolean removed = mutes.removeIf(m -> m.getTarget().equalsIgnoreCase(targetLower));
        if (removed) {
            save();
        }
        return removed;
    }

    public synchronized MuteEntry getMute(String targetLower) {
        purgeExpired();
        for (MuteEntry entry : mutes) {
            if (entry.getTarget().equalsIgnoreCase(targetLower)) {
                return entry;
            }
        }
        return null;
    }

    public synchronized List<MuteEntry> list(String search) {
        purgeExpired();
        List<MuteEntry> result = new ArrayList<>();
        for (MuteEntry entry : mutes) {
            if (search != null && !search.isEmpty() && !entry.getDisplay().toLowerCase().contains(search.toLowerCase())) {
                continue;
            }
            result.add(entry);
        }
        return result;
    }
}
