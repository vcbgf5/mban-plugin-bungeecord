package com.dziubek.banmanager;

import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.plugin.Command;

import java.util.List;

/** /mutelist [fraza] - lista aktualnie wyciszonych, opcjonalnie filtrowalna po nicku. */
public class MuteListCommand extends Command {

    private static final String PERMISSION = "banmanager.mute";

    private final BanManagerPlugin plugin;

    public MuteListCommand(BanManagerPlugin plugin) {
        super("mutelist", null);
        this.plugin = plugin;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (!Permissions.has(sender, PERMISSION)) {
            sender.sendMessage(TextComponent.fromLegacyText("§cNie masz uprawnień do tej komendy."));
            return;
        }
        String search = args.length > 0 ? String.join(" ", args) : null;
        List<MuteManager.MuteEntry> entries = plugin.getMutes().list(search);
        if (entries.isEmpty()) {
            sender.sendMessage(TextComponent.fromLegacyText("§eBrak wyciszonych graczy."));
            return;
        }
        sender.sendMessage(TextComponent.fromLegacyText("§8§m----§r §e§lWyciszeni §8§m---- §7(" + entries.size() + ")"));
        for (MuteManager.MuteEntry entry : entries) {
            sender.sendMessage(TextComponent.fromLegacyText("§7- §f" + entry.getDisplay() + " §7- §f"
                    + DurationParser.format(entry.getExpiresAt()) + " §7- §f" + entry.getReason() + " §8(przez " + entry.getBy() + ")"));
        }
    }
}
