package com.dziubek.banmanager;

import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

/** /vanish - ukrywa admina z /online. To NIE prawdziwa niewidzialność na serwerach (proxy tego nie kontroluje), tylko z list tego pluginu. */
public class VanishCommand extends Command {

    private static final String PERMISSION = "banmanager.vanish";

    private final BanManagerPlugin plugin;

    public VanishCommand(BanManagerPlugin plugin) {
        super("vanish", null);
        this.plugin = plugin;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (!Permissions.has(sender, PERMISSION)) {
            sender.sendMessage(TextComponent.fromLegacyText("§cNie masz uprawnień do tej komendy."));
            return;
        }
        if (!(sender instanceof ProxiedPlayer player)) {
            sender.sendMessage(TextComponent.fromLegacyText("§cTej komendy może użyć tylko gracz."));
            return;
        }
        boolean nowVanished = plugin.getVanish().toggle(player.getUniqueId());
        sender.sendMessage(TextComponent.fromLegacyText(nowVanished
                ? "§7Jesteś teraz ukryty z §f/online§7. §8(To nie prawdziwa niewidzialność na serwerach.)"
                : "§aJesteś znowu widoczny w §f/online§a."));
    }
}
