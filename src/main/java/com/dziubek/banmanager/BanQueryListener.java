package com.dziubek.banmanager;

import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.connection.Server;
import net.md_5.bungee.api.event.PluginMessageEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 * Odpowiada na zapytania pluginow po stronie Bukkit (np. LobbySpawn, kanal "banmanager:query")
 * czy dany gracz ma ban - zeby lobby moglo pokazac mu powod/czas/kto zbanowal ZANIM w ogole
 * sprobuje go wyslac na zbanowany serwer, zamiast polegac tylko na ServerConnectEvent po fakcie.
 *
 * Protokol: Bukkit wysyla UTF z nazwa docelowego serwera; proxy odsyla UTF (ta sama nazwa, do
 * dopasowania odpowiedzi po stronie Bukkit), boolean (czy zbanowany), a jesli tak - UTF zasieg
 * ("GLOBAL" albo nazwa serwera), UTF powod, UTF kto zbanowal, long expires-at (-1 = na zawsze),
 * a na koncu boolean - czy docelowy serwer jest zamkniety (/shutdown - "prace techniczne").
 */
public class BanQueryListener implements Listener {

    private static final String CHANNEL = "banmanager:query";

    private final BanManagerPlugin plugin;

    public BanQueryListener(BanManagerPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPluginMessage(PluginMessageEvent event) {
        if (!event.getTag().equals(CHANNEL) || !(event.getSender() instanceof Server)) {
            return;
        }
        event.setCancelled(true);

        ProxiedPlayer player = findPlayer((Server) event.getSender());
        if (player == null) {
            return;
        }

        try {
            DataInputStream in = new DataInputStream(new ByteArrayInputStream(event.getData()));
            String targetServer = in.readUTF();

            BanEntry ban = plugin.getStorage().findBlocking(BanEntry.Type.PLAYER, player.getName().toLowerCase(), targetServer);
            String ip = MenuHelper.ipOf(player);
            if (ban == null && ip != null) {
                ban = plugin.getStorage().findBlocking(BanEntry.Type.IP, ip, targetServer);
            }

            ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
            DataOutputStream out = new DataOutputStream(byteOut);
            out.writeUTF(targetServer);
            out.writeBoolean(ban != null);
            if (ban != null) {
                out.writeUTF(ban.isGlobal() ? "GLOBAL" : ban.getScope());
                out.writeUTF(ban.getReason());
                out.writeUTF(ban.getBy());
                out.writeLong(ban.getExpiresAt());
            }
            out.writeBoolean(plugin.getMaintenance().isClosed(targetServer));
            player.sendData(CHANNEL, byteOut.toByteArray());
        } catch (IOException e) {
            plugin.getLogger().warning("Blad odczytu zapytania o ban: " + e.getMessage());
        }
    }

    /** Server (połączenie backendowe) jest 1:1 per gracz - dopasowanie po referencji. */
    private ProxiedPlayer findPlayer(Server server) {
        for (ProxiedPlayer candidate : ProxyServer.getInstance().getPlayers()) {
            if (candidate.getServer() == server) {
                return candidate;
            }
        }
        return null;
    }
}
