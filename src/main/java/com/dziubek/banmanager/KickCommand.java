package com.dziubek.banmanager;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.TabExecutor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/** /kick <gracz> [powód] - natychmiastowe rozłączenie z proxy, BEZ bana (gracz może wejść od razu ponownie). */
public class KickCommand extends Command implements TabExecutor {

    private static final String PERMISSION = "banmanager.kick";

    public KickCommand() {
        super("kick", null);
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (!Permissions.has(sender, PERMISSION)) {
            sender.sendMessage(TextComponent.fromLegacyText("§cNie masz uprawnień do tej komendy."));
            return;
        }
        if (args.length == 0) {
            sender.sendMessage(TextComponent.fromLegacyText("§cUżycie: /kick <gracz> [powód]"));
            return;
        }
        ProxiedPlayer target = ProxyServer.getInstance().getPlayer(args[0]);
        if (target == null) {
            sender.sendMessage(TextComponent.fromLegacyText("§cGracz '" + args[0] + "' nie jest online."));
            return;
        }
        String reason = args.length > 1
                ? ChatColor.translateAlternateColorCodes('&', String.join(" ", Arrays.copyOfRange(args, 1, args.length)))
                : "Wyrzucony przez administratora";
        String by = sender instanceof ProxiedPlayer ? sender.getName() : "Console";
        target.disconnect(TextComponent.fromLegacyText("§c§lWYRZUCONY\n§7Przez: §f" + by + "\n§7Powód: §f" + reason));
        sender.sendMessage(TextComponent.fromLegacyText("§aWyrzucono §f" + target.getName() + "§a."));
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
