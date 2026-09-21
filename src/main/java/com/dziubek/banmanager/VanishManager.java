package com.dziubek.banmanager;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Zbiór graczy "ukrytych" z /online - w pamięci, resetuje się przy restarcie. To NIE prawdziwa
 * niewidzialność (proxy nie kontroluje widoczności graczy na backendzie, to sprawa Bukkit/Paper) -
 * tylko ukrycie z list generowanych przez ten plugin.
 */
public class VanishManager {

    private final Set<UUID> vanished = ConcurrentHashMap.newKeySet();

    /** @return nowy stan (true = teraz ukryty) */
    public boolean toggle(UUID uuid) {
        if (vanished.remove(uuid)) {
            return false;
        }
        vanished.add(uuid);
        return true;
    }

    public boolean isVanished(UUID uuid) {
        return vanished.contains(uuid);
    }
}
