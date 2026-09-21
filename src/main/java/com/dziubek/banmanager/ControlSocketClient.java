package com.dziubek.banmanager;

import net.md_5.bungee.api.config.ServerInfo;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

/**
 * Awaryjny transport dla banmanager:control, gdy na docelowym serwerze nie ma ani jednego gracza -
 * zwykły plugin messaging (patrz ControlChannel) wymaga połączenia gracza jako transportu, więc
 * bez niego np. realny SHUTDOWN wydany na pustym serwerze nigdy by nie dotarł. Łączy się więc
 * wprost gniazdem TCP do BackendManagera na tym samym hoście co serwer gry, na porcie gry +
 * control-port-offset, podając control-secret (musi być identyczny po obu stronach - patrz
 * config.yml tego pluginu i config.yml BackendManagera).
 */
public final class ControlSocketClient {

    private ControlSocketClient() {
    }

    public static boolean send(BanManagerPlugin plugin, ServerInfo info, String line) {
        InetSocketAddress address = info.getAddress();
        int port = address.getPort() + plugin.getControlPortOffset();
        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress(address.getHostString(), port), 2000);
            socket.setSoTimeout(2000);
            OutputStream out = socket.getOutputStream();
            out.write((plugin.getControlSecret() + "\n" + line + "\n").getBytes(StandardCharsets.UTF_8));
            out.flush();
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
            return "OK".equals(in.readLine());
        } catch (IOException e) {
            plugin.getLogger().warning("Nie udało się połączyć z BackendManagerem na " + info.getName()
                    + " (" + address.getHostString() + ":" + port + "): " + e.getMessage()
                    + " - jeśli to pusty serwer, upewnij się że ma zainstalowany BackendManager z tym samym control-secret.");
            return false;
        }
    }
}
