package com.dziubek.banmanager;

import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.connection.Server;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 * Kanal "banmanager:control" - proxy wysyła polecenia do BackendManagera (pluginu Paper na
 * każdym serwerze): realne ukrycie gracza (hidePlayer/showPlayer) i realne wyłączenie procesu
 * (getServer().shutdown()). Zwykły plugin messaging wymaga połączenia gracza jako transportu -
 * gdy na docelowym serwerze nie ma ani jednego gracza (typowy przypadek dla /shutdown wydanego
 * z wyprzedzeniem, zanim ktokolwiek dołączy), sendShutdown spada na awaryjny transport wprost
 * gniazdem TCP (ControlSocketClient), żeby sygnał i tak dotarł.
 */
public final class ControlChannel {

    public static final String CHANNEL = "banmanager:control";

    private ControlChannel() {
    }

    public static void sendVanish(ProxiedPlayer player, boolean vanished) {
        Server server = player.getServer();
        if (server == null) {
            return;
        }
        try {
            ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
            DataOutputStream out = new DataOutputStream(byteOut);
            out.writeUTF(vanished ? "VANISH_ON" : "VANISH_OFF");
            out.writeUTF(player.getName());
            out.writeUTF(player.getUniqueId().toString());
            server.sendData(CHANNEL, byteOut.toByteArray());
        } catch (IOException e) {
            // brak transportu (np. serwer się właśnie restartuje) - nie ma jak wysłać, pomijamy
        }
    }

    /**
     * Wysyła do serwera - jeśli są tam gracze, przez plugin messaging (transportem jest jeden
     * z nich); jeśli serwer jest PUSTY, plugin messaging nie ma jak zadziałać, więc od razu
     * spada na awaryjne gniazdo TCP (ControlSocketClient), w tle, żeby nie blokować proxy.
     */
    public static void sendShutdown(BanManagerPlugin plugin, ServerInfo info, String reason) {
        boolean deliveredViaPlayer = false;
        if (!info.getPlayers().isEmpty()) {
            try {
                ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
                DataOutputStream out = new DataOutputStream(byteOut);
                out.writeUTF("SHUTDOWN");
                out.writeUTF(reason);
                info.sendData(CHANNEL, byteOut.toByteArray());
                deliveredViaPlayer = true;
            } catch (IOException e) {
                // spadamy do awaryjnego TCP poniżej
            }
        }
        if (!deliveredViaPlayer) {
            ProxyServer.getInstance().getScheduler().runAsync(plugin,
                    () -> ControlSocketClient.send(plugin, info, "SHUTDOWN\t" + reason));
        }
    }
}
