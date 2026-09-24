package com.dziubek.banmanager;

import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.plugin.Command;

/** /reloadicon - na nowo wczytuje plugins/BanManager/servericon/icon.png bez restartu proxy. */
public class ReloadIconCommand extends Command {

    private static final String PERMISSION = "banmanager.reload";

    private final BanManagerPlugin plugin;

    public ReloadIconCommand(BanManagerPlugin plugin) {
        super("reloadicon", null);
        this.plugin = plugin;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (!Permissions.has(sender, PERMISSION)) {
            sender.sendMessage(TextComponent.fromLegacyText("§cNie masz uprawnień do tej komendy."));
            return;
        }
        plugin.getServerIcon().load();
        boolean active = plugin.getServerIcon().getFavicon() != null;
        sender.sendMessage(TextComponent.fromLegacyText(active
                ? "§aIkona serwera przeładowana - wymuszana na kazdym pingu."
                : "§eBrak pliku icon.png w plugins/BanManager/servericon/ - wgraj go i spróbuj ponownie."));
    }
}
