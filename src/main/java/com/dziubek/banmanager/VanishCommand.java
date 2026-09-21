package com.dziubek.banmanager;

import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

/**
 * /vanish - ukrywa admina z /online ORAZ wysyła sygnał VANISH_ON/OFF kanałem "banmanager:control"
 * do BackendManagera na serwerze, na którym admin aktualnie jest - jeśli backend ma zainstalowany
 * BackendManager, gracz naprawdę znika z widoku innych (hidePlayer), poza graczami z uprawnieniem
 * "admin.wholeserver". Bez BackendManagera na danym serwerze sygnał po prostu nie ma tam efektu -
 * gracz i tak zniknie z /online tego pluginu.
 */
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
        ControlChannel.sendVanish(player, nowVanished);
        sender.sendMessage(TextComponent.fromLegacyText(nowVanished
                ? "§7Jesteś teraz ukryty - znikasz z §f/online§7 i z widoku graczy bez §fadmin.wholeserver§7."
                : "§aJesteś znowu widoczny w §f/online§a i na serwerze."));
    }
}
