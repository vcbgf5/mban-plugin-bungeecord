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

/** /warn <gracz> <powód> - loguje ostrzeżenie (patrz /warnings) i informuje gracza, jeśli online. */
public class WarnCommand extends Command implements TabExecutor {

    private static final String PERMISSION = "banmanager.warn";

    private final BanManagerPlugin plugin;

    public WarnCommand(BanManagerPlugin plugin) {
        super("warn", null);
        this.plugin = plugin;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (!Permissions.has(sender, PERMISSION)) {
            sender.sendMessage(TextComponent.fromLegacyText("§cNie masz uprawnień do tej komendy."));
            return;
        }
        if (args.length < 2) {
            sender.sendMessage(TextComponent.fromLegacyText("§cUżycie: /warn <gracz> <powód>"));
            return;
        }
        String reason = String.join(" ", Arrays.copyOfRange(args, 1, args.length));
        String by = sender instanceof ProxiedPlayer ? sender.getName() : "Console";

        ProxiedPlayer online = ProxyServer.getInstance().getPlayer(args[0]);
        String display = online != null ? online.getName() : args[0];
        plugin.getWarnings().warn(display.toLowerCase(), reason, by);

        int total = plugin.getWarnings().get(display.toLowerCase()).size();
        sender.sendMessage(TextComponent.fromLegacyText("§aOstrzeżono §f" + display + " §7(łącznie: §f" + total + "§7) - §f" + reason));
        if (online != null) {
            online.sendMessage(TextComponent.fromLegacyText("§e⚠ Otrzymałeś ostrzeżenie: §f" + reason));
        }
    }

    @Override
    public Iterable<String> onTabComplete(CommandSender sender, String[] args) {
        List<String> options = new ArrayList<>();
        if (args.length <= 1) {
            for (ProxiedPlayer player : ProxyServer.getInstance().getPlayers()) {
                options.add(player.getName());
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
