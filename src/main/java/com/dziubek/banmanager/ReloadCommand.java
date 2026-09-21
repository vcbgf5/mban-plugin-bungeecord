package com.dziubek.banmanager;

import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.plugin.Command;

/** /bmreload - przeładowuje config.yml oraz na nowo wczytuje bans.yml/mutes.yml z dysku (na wypadek ręcznej edycji). */
public class ReloadCommand extends Command {

    private static final String PERMISSION = "banmanager.reload";

    private final BanManagerPlugin plugin;

    public ReloadCommand(BanManagerPlugin plugin) {
        super("bmreload", null);
        this.plugin = plugin;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (!Permissions.has(sender, PERMISSION)) {
            sender.sendMessage(TextComponent.fromLegacyText("§cNie masz uprawnień do tej komendy."));
            return;
        }
        plugin.reload();
        sender.sendMessage(TextComponent.fromLegacyText("§aPrzeładowano config.yml oraz dane banów/wyciszeń."));
    }
}
