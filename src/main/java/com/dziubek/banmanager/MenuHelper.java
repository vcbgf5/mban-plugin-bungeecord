package com.dziubek.banmanager;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.net.InetSocketAddress;
import java.net.SocketAddress;

/** Wspolna logika menu na czacie i wiadomosci bana - uzywana przez /mban, /mbanip i BanListener. */
public final class MenuHelper {

    private MenuHelper() {
    }

    public static void sendScopeMenu(ProxiedPlayer sender, String command, String target) {
        ChatMenuUtil.sendHeader(sender, "&eWybierz zasieg dla &f" + target);
        ChatMenuUtil.sendButton(sender, "&c[GLOBAL]", "&7Ban na WSZYSTKICH serwerach naraz",
                "/" + command + " " + target + " global ");
        for (String server : ProxyServer.getInstance().getServers().keySet()) {
            ServerInfo info = ProxyServer.getInstance().getServerInfo(server);
            int online = info != null ? info.getPlayers().size() : 0;
            ChatMenuUtil.sendButton(sender, "&6[" + server + "]", "&7Ban tylko na tym serwerze &8(&f" + online + " online&8)",
                    "/" + command + " " + target + " " + server + " ");
        }
    }

    public static void sendDurationMenu(ProxiedPlayer sender, String command, String target, String scope) {
        ChatMenuUtil.sendHeader(sender, "&eWybierz czas trwania");
        String[][] presets = {
                {"1h", "1 godzina"}, {"1d", "1 dzien"}, {"7d", "7 dni"}, {"30d", "30 dni"}, {"perm", "NA ZAWSZE"}
        };
        for (String[] preset : presets) {
            ChatMenuUtil.sendButton(sender, "&b[" + preset[0] + "]", "&7" + preset[1],
                    "/" + command + " " + target + " " + scope + " " + preset[0] + " ");
        }
    }

    public static void sendReasonMenu(ProxiedPlayer sender, String command, String target, String scope, String duration) {
        ChatMenuUtil.sendHeader(sender, "&eWybierz powod (albo dopisz wlasny na koncu)");
        for (String reason : ReasonPresets.REASONS) {
            ChatMenuUtil.sendButton(sender, "&d[" + reason + "]", "&7Kliknij, aby wstawic ten powod",
                    "/" + command + " " + target + " " + scope + " " + duration + " " + reason);
        }
    }

    /** Wiadomosc pokazywana zbanowanemu - na jakim zasiegu, na ile, przez kogo i za co. */
    public static TextComponent banMessage(BanEntry entry) {
        String scope = entry.isGlobal() ? "CALYM proxy (wszystkie serwery)" : "serwerze " + entry.getScope();
        String text = "&4&lZOSTALES ZBANOWANY\n"
                + "&7Zasieg: &f" + scope + "\n"
                + "&7Czas: &f" + DurationParser.format(entry.getExpiresAt()) + "\n"
                + "&7Przez: &f" + entry.getBy() + "\n"
                + "&7Powod: &f" + entry.getReason();
        return new TextComponent(ChatColor.translateAlternateColorCodes('&', text));
    }

    public static String ipOf(ProxiedPlayer player) {
        SocketAddress address = player.getAddress();
        if (address instanceof InetSocketAddress inet) {
            return inet.getAddress().getHostAddress();
        }
        return null;
    }

    /**
     * Pierwszy serwer (preferowany "fallback-server" z configu, inaczej pierwszy z brzegu) inny
     * niz `excluded`, na ktorym `player` (ani jego nick, ani jego IP) nie ma aktywnego bana -
     * do przekierowania juz polaczonego gracza, ktory dostal ban na serwerze, na ktorym akurat jest.
     */
    public static ServerInfo findAlternative(String excluded, ProxiedPlayer player, BanManagerPlugin plugin) {
        String nameLower = player.getName().toLowerCase();
        String ip = ipOf(player);

        String fallback = plugin.getFallbackServer();
        if (fallback != null && !fallback.isEmpty() && !fallback.equalsIgnoreCase(excluded)) {
            ServerInfo info = ProxyServer.getInstance().getServerInfo(fallback);
            if (info != null && !isBlocked(plugin, fallback, nameLower, ip)) {
                return info;
            }
        }
        for (ServerInfo info : ProxyServer.getInstance().getServers().values()) {
            if (info.getName().equalsIgnoreCase(excluded)) {
                continue;
            }
            if (!isBlocked(plugin, info.getName(), nameLower, ip)) {
                return info;
            }
        }
        return null;
    }

    private static boolean isBlocked(BanManagerPlugin plugin, String server, String nameLower, String ip) {
        if (plugin.getStorage().findBlocking(BanEntry.Type.PLAYER, nameLower, server) != null) {
            return true;
        }
        return ip != null && plugin.getStorage().findBlocking(BanEntry.Type.IP, ip, server) != null;
    }

    /**
     * Jak findAlternative, ale dla zamkniec (/shutdown, /pracetech, MaintenanceListener) - zamiast
     * sprawdzac bany, sprawdza ktory serwer NIE jest zamkniety. Preferuje skonfigurowany
     * "fallback-server" - bez tego pierwszy z brzegu serwer z pelnej listy zarejestrowanych na
     * proxy moze trafic na serwer infrastrukturalny (np. limbo), a nie na prawdziwe lobby.
     */
    public static ServerInfo findOpenAlternative(String excluded, BanManagerPlugin plugin) {
        String fallback = plugin.getFallbackServer();
        if (fallback != null && !fallback.isEmpty() && !fallback.equalsIgnoreCase(excluded)) {
            ServerInfo info = ProxyServer.getInstance().getServerInfo(fallback);
            if (info != null && !plugin.getMaintenance().isClosed(fallback)) {
                return info;
            }
        }
        for (ServerInfo info : ProxyServer.getInstance().getServers().values()) {
            if (info.getName().equalsIgnoreCase(excluded)) {
                continue;
            }
            if (!plugin.getMaintenance().isClosed(info.getName())) {
                return info;
            }
        }
        return null;
    }
}
