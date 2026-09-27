package com.dziubek.banmanager;

import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.event.ServerConnectEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import net.md_5.bungee.event.EventPriority;

import java.net.InetSocketAddress;
import java.util.Locale;

/**
 * Przekierowanie po subdomenie: gracz łączący się przez np. "test.vantanet.pl" trafia od razu
 * na serwer proxy o nazwie "test" (jeśli taki istnieje), zamiast na domyślny serwer z
 * config.yml Bungee/Waterfalla. Wymaga, żeby DNS subdomeny wskazywał na ten sam adres IP co
 * główna domena (rekord A/CNAME "*.vantanet.pl" albo pojedynczy wpis per subdomena) - to jest
 * po stronie hostingu/DNS, nie tego pluginu.
 * Działa tylko na PIERWSZYM połączeniu z proxy (Reason.JOIN_PROXY) - nie nadpisuje późniejszych
 * ręcznych /server ani przekierowań z powodu zamknięcia serwera (MaintenanceListener/BanListener
 * i tak widzą już podmieniony cel, bo ten listener ma niższy priorytet i odpala się pierwszy).
 */
public class SubdomainRouteListener implements Listener {

    private final BanManagerPlugin plugin;

    public SubdomainRouteListener(BanManagerPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onServerConnect(ServerConnectEvent event) {
        if (event.getReason() != ServerConnectEvent.Reason.JOIN_PROXY) {
            return;
        }
        if (!plugin.isSubdomainRoutingEnabled()) {
            return;
        }
        String baseDomain = plugin.getSubdomainBaseDomain();
        if (baseDomain.isEmpty()) {
            return;
        }

        InetSocketAddress virtualHost = event.getPlayer().getPendingConnection().getVirtualHost();
        if (virtualHost == null) {
            return;
        }

        String host = virtualHost.getHostString().toLowerCase(Locale.ROOT);
        if (host.endsWith(".")) {
            host = host.substring(0, host.length() - 1);
        }
        String suffix = "." + baseDomain.toLowerCase(Locale.ROOT);
        if (!host.endsWith(suffix)) {
            return;
        }

        String subdomain = host.substring(0, host.length() - suffix.length());
        if (subdomain.isEmpty() || subdomain.contains(".")) {
            return;
        }

        ServerInfo target = ProxyServer.getInstance().getServerInfo(subdomain);
        if (target != null) {
            event.setTarget(target);
        }
    }
}
