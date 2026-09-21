package com.dziubek.banmanager;

import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.PendingConnection;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.LoginEvent;
import net.md_5.bungee.api.event.ServerConnectEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

import java.net.InetSocketAddress;
import java.net.SocketAddress;

/**
 * Faktyczne egzekwowanie banow: LoginEvent blokuje wejscie na PROXY graczom z banem globalnym
 * (nazwa lub IP), ServerConnectEvent blokuje/przekierowuje przy probie wejscia na KONKRETNY
 * serwer, na ktorym gracz ma ban ograniczony tylko do niego.
 */
public class BanListener implements Listener {

    private final BanManagerPlugin plugin;

    public BanListener(BanManagerPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onLogin(LoginEvent event) {
        PendingConnection connection = event.getConnection();
        String name = connection.getName().toLowerCase();
        String ip = ipOf(connection.getAddress());

        BanEntry ban = plugin.getStorage().findBlocking(BanEntry.Type.PLAYER, name, null);
        if (ban == null && ip != null) {
            ban = plugin.getStorage().findBlocking(BanEntry.Type.IP, ip, null);
        }
        if (ban != null) {
            event.setCancelled(true);
            event.setCancelReason(MenuHelper.banMessage(ban));
        }
    }

    @EventHandler
    public void onServerConnect(ServerConnectEvent event) {
        ProxiedPlayer player = event.getPlayer();
        String server = event.getTarget().getName();
        String name = player.getName().toLowerCase();
        String ip = ipOf(player.getAddress());

        BanEntry ban = plugin.getStorage().findBlocking(BanEntry.Type.PLAYER, name, server);
        if (ban == null && ip != null) {
            ban = plugin.getStorage().findBlocking(BanEntry.Type.IP, ip, server);
        }
        if (ban == null) {
            return;
        }

        ServerInfo alt = MenuHelper.findAlternative(server, player, plugin);
        if (alt != null) {
            player.sendMessage(MenuHelper.banMessage(ban));
            event.setTarget(alt);
            return;
        }

        event.setCancelled(true);
        if (player.getServer() != null) {
            // gracz jest juz gdzies polaczony (np. przelaczanie serwerow) - nie ma dokad go przekierowac, zostaje tam gdzie jest
            player.sendMessage(MenuHelper.banMessage(ban));
        } else {
            // to bylo jego POCZATKOWE polaczenie i nie ma zadnego innego serwera do wpuszczenia - nie ma innej opcji niz rozlaczyc
            player.disconnect(MenuHelper.banMessage(ban));
        }
    }

    private String ipOf(SocketAddress address) {
        if (address instanceof InetSocketAddress inet) {
            return inet.getAddress().getHostAddress();
        }
        return null;
    }
}
