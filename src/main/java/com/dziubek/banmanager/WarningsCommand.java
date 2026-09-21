package com.dziubek.banmanager;

import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.plugin.Command;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/** /warnings <gracz> - historia ostrzeżeń danego gracza (patrz /warn). */
public class WarningsCommand extends Command {

    private static final String PERMISSION = "banmanager.warn";

    private final BanManagerPlugin plugin;

    public WarningsCommand(BanManagerPlugin plugin) {
        super("warnings", null);
        this.plugin = plugin;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (!Permissions.has(sender, PERMISSION)) {
            sender.sendMessage(TextComponent.fromLegacyText("§cNie masz uprawnień do tej komendy."));
            return;
        }
        if (args.length == 0) {
            sender.sendMessage(TextComponent.fromLegacyText("§cUżycie: /warnings <gracz>"));
            return;
        }
        List<WarnManager.Warning> list = plugin.getWarnings().get(args[0].toLowerCase());
        if (list.isEmpty()) {
            sender.sendMessage(TextComponent.fromLegacyText("§eGracz '" + args[0] + "' nie ma ostrzeżeń."));
            return;
        }
        SimpleDateFormat format = new SimpleDateFormat("dd.MM HH:mm");
        sender.sendMessage(TextComponent.fromLegacyText("§8§m----§r §e§lOstrzeżenia: " + args[0] + " §8§m---- §7(" + list.size() + ")"));
        for (WarnManager.Warning warning : list) {
            sender.sendMessage(TextComponent.fromLegacyText("§7[" + format.format(new Date(warning.at())) + "] §f"
                    + warning.reason() + " §8(przez " + warning.by() + ")"));
        }
    }
}
