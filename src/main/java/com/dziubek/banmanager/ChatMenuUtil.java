package com.dziubek.banmanager;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.hover.content.Text;
import net.md_5.bungee.api.connection.ProxiedPlayer;

/**
 * Proxy nie ma GUI (skrzynek/inventory), wiec zamiennikiem jest interaktywny czat: kazdy
 * "przycisk" to kawalek tekstu z podpowiedzia po najechaniu (hover) i komenda WSTAWIANA do
 * wpisania po kliknieciu (SUGGEST_COMMAND, nie wykonywana od razu) - admin widzi i moze
 * poprawic zanim wyśle.
 */
public final class ChatMenuUtil {

    private ChatMenuUtil() {
    }

    public static void sendHeader(ProxiedPlayer player, String text) {
        player.sendMessage(new TextComponent(ChatColor.translateAlternateColorCodes('&', "&8&m----&r " + text + " &8&m----")));
    }

    public static void sendButton(ProxiedPlayer player, String label, String hoverText, String suggestCommand) {
        player.sendMessage(button(label, hoverText, suggestCommand));
    }

    public static TextComponent button(String label, String hoverText, String suggestCommand) {
        TextComponent component = new TextComponent(ChatColor.translateAlternateColorCodes('&', label));
        component.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                new Text(TextComponent.fromLegacyText(ChatColor.translateAlternateColorCodes('&', hoverText)))));
        component.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, suggestCommand));
        return component;
    }
}
