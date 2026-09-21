package com.dziubek.banmanager;

import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.connection.Server;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 * Kanal "banmanager:control" - proxy wysyła polecenia do BackendManagera (pluginu Paper na
 * każdym serwerze): realne ukrycie gracza (hidePlayer/showPlayer) i realne wyłączenie procesu
 * (getServer().shutdown()). Transportem plugin messaging jest zawsze połączenie gracza - dlatego
 * SHUTDOWN trzeba wysłać ZANIM ostatni gracz zostanie przeniesiony/rozłączony z danego serwera.
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

    /** Wysyła do KAŻDEGO gracza aktualnie na danym serwerze - musi trafić do zanim ostatni wyjdzie. */
    public static void sendShutdown(ServerInfo info, String reason) {
        try {
            ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
            DataOutputStream out = new DataOutputStream(byteOut);
            out.writeUTF("SHUTDOWN");
            out.writeUTF(reason);
            info.sendData(CHANNEL, byteOut.toByteArray());
        } catch (IOException e) {
            // brak podłączonych graczy na tym serwerze - sygnał i tak nie ma jak dotrzeć
        }
    }
}
