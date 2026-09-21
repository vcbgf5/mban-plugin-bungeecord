package com.dziubek.banmanager;

import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.TabExecutor;

import java.util.ArrayList;
import java.util.List;

/** /unmute <gracz> - zdejmuje wyciszenie. */
public class UnmuteCommand extends Command implements TabExecutor {

    private static final String PERMISSION = "banmanager.mute";

    private final BanManagerPlugin plugin;

    public UnmuteCommand(BanManagerPlugin plugin) {
        super("unmute", null);
        this.plugin = plugin;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (!Permissions.has(sender, PERMISSION)) {
            sender.sendMessage(TextComponent.fromLegacyText("§cNie masz uprawnień do tej komendy."));
            return;
        }
        if (args.length == 0) {
            sender.sendMessage(TextComponent.fromLegacyText("§cUżycie: /unmute <gracz>"));
            return;
        }
        boolean removed = plugin.getMutes().unmute(args[0].toLowerCase());
        sender.sendMessage(TextComponent.fromLegacyText(removed
                ? "§aZdjęto wyciszenie graczowi §f" + args[0]
                : "§cGracz '" + args[0] + "' nie jest wyciszony."));
    }

    @Override
    public Iterable<String> onTabComplete(CommandSender sender, String[] args) {
        List<String> options = new ArrayList<>();
        if (args.length <= 1) {
            for (MuteManager.MuteEntry entry : plugin.getMutes().list(null)) {
                options.add(entry.getDisplay());
            }
        }
        String current = args.length > 0 ? args[args.length - 1].toLowerCase() : "";
        List<String> filtered = new ArrayList<>();
        for (String option : options) {
            if (option.toLowerCase().startsWith(current)) {
                filtered.add(option);
            }
        }
        return filtered;
    }
}
