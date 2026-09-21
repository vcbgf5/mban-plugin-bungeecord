package com.dziubek.banmanager;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Ostrzeżenia - lekki log historii (nie egzekwowany automatycznie, żadnej kary sam z siebie nie
 * nakłada), do śledzenia zachowania gracza przez staff. W PAMIĘCI - nie przetrwa restartu proxy
 * (to nieformalna notatka, nie trwały system kar jak bany/wyciszenia).
 */
public class WarnManager {

    public record Warning(String reason, String by, long at) {
    }

    private final Map<String, List<Warning>> warnings = new ConcurrentHashMap<>();

    public void warn(String targetLower, String reason, String by) {
        warnings.computeIfAbsent(targetLower, k -> new ArrayList<>()).add(new Warning(reason, by, System.currentTimeMillis()));
    }

    public List<Warning> get(String targetLower) {
        return warnings.getOrDefault(targetLower, List.of());
    }
}
