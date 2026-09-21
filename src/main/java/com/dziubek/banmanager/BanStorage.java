package com.dziubek.banmanager;

import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Trzyma wszystkie aktywne bany (graczy i IP) w jednym pliku bans.yml. Kazdy wpis ma numeryczny
 * id, typ (PLAYER/IP), zasieg (GLOBAL albo dokladna nazwa serwera z proxy) i dane bana. Wygasle
 * bany czasowe sa czyszczone leniwie - przy kazdym odczycie, bez osobnego watku/timera.
 */
public class BanStorage {

    private final BanManagerPlugin plugin;
    private final File file;
    private final List<BanEntry> bans = new ArrayList<>();
    private int nextId = 1;

    public BanStorage(BanManagerPlugin plugin) {
        this.plugin = plugin;
        if (!plugin.getDataFolder().exists()) {
            plugin.getDataFolder().mkdirs();
        }
        this.file = new File(plugin.getDataFolder(), "bans.yml");
    }

    public void load() {
        Configuration data;
        try {
            if (!file.exists()) {
                file.createNewFile();
            }
            data = ConfigurationProvider.getProvider(YamlConfiguration.class).load(file);
        } catch (IOException e) {
            plugin.getLogger().warning("Nie udalo sie wczytac bans.yml: " + e.getMessage());
            data = new Configuration();
        }

        bans.clear();
        nextId = data.getInt("next-id", 1);
        Configuration bansSection = data.getSection("bans");
        if (bansSection != null) {
            for (String key : bansSection.getKeys()) {
                Configuration entry = bansSection.getSection(key);
                try {
                    BanEntry.Type type = BanEntry.Type.valueOf(entry.getString("type"));
                    BanEntry banEntry = new BanEntry(
                            Integer.parseInt(key),
                            type,
                            entry.getString("target"),
                            entry.getString("display", entry.getString("target")),
                            entry.getString("scope"),
                            entry.getString("reason", "Brak podanego powodu"),
                            entry.getString("by", "Console"),
                            entry.getLong("created-at"),
                            entry.getLong("expires-at", -1)
                    );
                    bans.add(banEntry);
                } catch (Exception e) {
                    plugin.getLogger().warning("Uszkodzony wpis bana '" + key + "' w bans.yml, pomijam.");
                }
            }
        }
    }

    /** Buduje CALKOWICIE nowy plik z aktualnej listy w pamieci - unika problemow z usuwaniem starych kluczy. */
    private synchronized void save() {
        Configuration fresh = new Configuration();
        fresh.set("next-id", nextId);
        for (BanEntry entry : bans) {
            String base = "bans." + entry.getId();
            fresh.set(base + ".type", entry.getType().name());
            fresh.set(base + ".target", entry.getTarget());
            fresh.set(base + ".display", entry.getDisplay());
            fresh.set(base + ".scope", entry.getScope());
            fresh.set(base + ".reason", entry.getReason());
            fresh.set(base + ".by", entry.getBy());
            fresh.set(base + ".created-at", entry.getCreatedAt());
            fresh.set(base + ".expires-at", entry.getExpiresAt());
        }
        try {
            ConfigurationProvider.getProvider(YamlConfiguration.class).save(fresh, file);
        } catch (IOException e) {
            plugin.getLogger().warning("Nie udalo sie zapisac bans.yml: " + e.getMessage());
        }
    }

    private boolean purgeExpired() {
        return bans.removeIf(BanEntry::isExpired);
    }

    /** Dodaje ban - jesli target ma juz ban o TAKIM SAMYM zasiegu, nadpisuje go (nie duplikuje). */
    public synchronized BanEntry addBan(BanEntry.Type type, String target, String display, String scope,
                                         String reason, String by, long durationMillis) {
        purgeExpired();
        bans.removeIf(b -> b.getType() == type && b.getTarget().equalsIgnoreCase(target) && b.getScope().equalsIgnoreCase(scope));

        long expiresAt = durationMillis < 0 ? -1 : System.currentTimeMillis() + durationMillis;
        BanEntry entry = new BanEntry(nextId++, type, target.toLowerCase(), display, scope, reason, by,
                System.currentTimeMillis(), expiresAt);
        bans.add(entry);
        save();
        return entry;
    }

    public synchronized boolean removeBan(BanEntry.Type type, String target, String scope) {
        purgeExpired();
        boolean removed = bans.removeIf(b -> b.getType() == type && b.getTarget().equalsIgnoreCase(target) && b.getScope().equalsIgnoreCase(scope));
        if (removed) {
            save();
        }
        return removed;
    }

    /** Wszystkie aktywne bany danego targetu (do menu przy /munban bez podanego zasiegu, gdy jest ich kilka). */
    public synchronized List<BanEntry> findActive(BanEntry.Type type, String target) {
        purgeExpired();
        List<BanEntry> result = new ArrayList<>();
        for (BanEntry entry : bans) {
            if (entry.getType() == type && entry.getTarget().equalsIgnoreCase(target)) {
                result.add(entry);
            }
        }
        return result;
    }

    /**
     * Ban globalny targetu (jesli ma) ALBO ban na konkretnym `server` (jesli podany, nie-null).
     * Uzywane do faktycznego egzekwowania - server=null sprawdza TYLKO bany globalne (np. login).
     */
    public synchronized BanEntry findBlocking(BanEntry.Type type, String target, String server) {
        purgeExpired();
        BanEntry serverBan = null;
        for (BanEntry entry : bans) {
            if (entry.getType() != type || !entry.getTarget().equalsIgnoreCase(target)) {
                continue;
            }
            if (entry.isGlobal()) {
                return entry;
            }
            if (server != null && entry.getScope().equalsIgnoreCase(server)) {
                serverBan = entry;
            }
        }
        return serverBan;
    }

    public synchronized List<BanEntry> list(String scopeFilter, String search) {
        purgeExpired();
        List<BanEntry> result = new ArrayList<>();
        for (BanEntry entry : bans) {
            if (scopeFilter != null && !scopeFilter.equalsIgnoreCase(entry.getScope())) {
                continue;
            }
            if (search != null && !search.isEmpty()) {
                String needle = search.toLowerCase();
                if (!entry.getDisplay().toLowerCase().contains(needle) && !entry.getReason().toLowerCase().contains(needle)) {
                    continue;
                }
            }
            result.add(entry);
        }
        return result;
    }

    public synchronized int count() {
        purgeExpired();
        return bans.size();
    }
}
