package com.dziubek.banmanager;

import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.TabExecutor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/** /mute <gracz> <czas> [powód] - wycisza na czacie (proxy-wide, egzekwowane przez MuteListener). */
public class MuteCommand extends Command implements TabExecutor {

    private static final String PERMISSION = "banmanager.mute";

    private final BanManagerPlugin plugin;

    public MuteCommand(BanManagerPlugin plugin) {
        super("mute", null);
        this.plugin = plugin;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (!Permissions.has(sender, PERMISSION)) {
            sender.sendMessage(TextComponent.fromLegacyText("§cNie masz uprawnień do tej komendy."));
            return;
        }
        if (args.length < 2) {
            sender.sendMessage(TextComponent.fromLegacyText("§cUżycie: /mute <gracz> <czas> [powód]"));
            return;
        }
        long durationMillis;
        try {
            durationMillis = DurationParser.parse(args[1]);
        } catch (IllegalArgumentException e) {
            sender.sendMessage(TextComponent.fromLegacyText("§cZły format czasu '" + args[1] + "'. Przykłady: 10m, 1h, 1d, perm"));
            return;
        }
        String reason = args.length > 2 ? String.join(" ", Arrays.copyOfRange(args, 2, args.length)) : "Brak podanego powodu";
        String by = sender instanceof ProxiedPlayer ? sender.getName() : "Console";

        ProxiedPlayer online = ProxyServer.getInstance().getPlayer(args[0]);
        String display = online != null ? online.getName() : args[0];

        MuteManager.MuteEntry entry = plugin.getMutes().mute(display.toLowerCase(), display, reason, by, durationMillis);
        sender.sendMessage(TextComponent.fromLegacyText("§aWyciszono §f" + display + " §7na §f"
                + DurationParser.format(entry.getExpiresAt()) + " §7- §f" + reason));
        if (online != null) {
            online.sendMessage(TextComponent.fromLegacyText("§cZostałeś wyciszony na czacie na §f"
                    + DurationParser.format(entry.getExpiresAt()) + "\n§7Powód: §f" + reason));
        }
    }

    @Override
    public Iterable<String> onTabComplete(CommandSender sender, String[] args) {
        List<String> options = new ArrayList<>();
        if (args.length <= 1) {
            for (ProxiedPlayer player : ProxyServer.getInstance().getPlayers()) {
                options.add(player.getName());
            }
        } else if (args.length == 2) {
            options.addAll(List.of("10m", "1h", "1d", "7d", "perm"));
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
