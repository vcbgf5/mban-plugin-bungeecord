package com.dziubek.banmanager;

import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.ChatEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

/**
 * Blokuje wiadomosci na czacie wyciszonym graczom. ChatEvent fires zarowno dla zwyklego czatu jak
 * i komend - blokujemy TYLKO zwykly czat (event.isCommand()==false), zeby wyciszenie nie
 * uniemozliwialo uzywania komend (np. /unmute nie mialoby sensu, gdyby /help tez bylo zablokowane).
 */
public class MuteListener implements Listener {

    private final BanManagerPlugin plugin;

    public MuteListener(BanManagerPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onChat(ChatEvent event) {
        if (event.isCommand() || !(event.getSender() instanceof ProxiedPlayer)) {
            return;
        }
        ProxiedPlayer player = (ProxiedPlayer) event.getSender();
        MuteManager.MuteEntry mute = plugin.getMutes().getMute(player.getName().toLowerCase());
        if (mute == null) {
            return;
        }
        event.setCancelled(true);
        String time = mute.isPermanent() ? "na zawsze" : DurationParser.format(mute.getExpiresAt());
        player.sendMessage(TextComponent.fromLegacyText("§cJesteś wyciszony na czacie §7- §c" + time
                + "\n§7Przez: §f" + mute.getBy() + " §7Powód: §f" + mute.getReason()));
    }
}
