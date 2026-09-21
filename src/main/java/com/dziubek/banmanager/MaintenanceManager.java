package com.dziubek.banmanager;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Trzyma serwery "zamknięte" (przez /shutdown) - tylko w pamięci, resetuje się przy restarcie
 * proxy (zamknięcie ma sens tylko dopóki trwa dana konserwacja/restart backendu, nie jest to
 * trwałe ustawienie do zapisywania na dysk).
 */
public class MaintenanceManager {

    private final Set<String> closedServers = ConcurrentHashMap.newKeySet();
    private volatile boolean allClosed = false;

    public void close(String server) {
        closedServers.add(server.toLowerCase());
    }

    public void reopen(String server) {
        closedServers.remove(server.toLowerCase());
    }

    public void closeAll() {
        allClosed = true;
    }

    public void reopenAll() {
        allClosed = false;
        closedServers.clear();
    }

    /** Zamknięty, jeśli konkretnie ten serwer jest zamknięty ALBO cały proxy jest zamknięty. */
    public boolean isClosed(String server) {
        return allClosed || closedServers.contains(server.toLowerCase());
    }

    public boolean isAllClosed() {
        return allClosed;
    }

    public Set<String> getClosedServers() {
        return closedServers;
    }
}
