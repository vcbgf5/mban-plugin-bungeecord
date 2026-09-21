package com.dziubek.banmanager;

import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.TabExecutor;

import java.util.ArrayList;
import java.util.List;

/** /whois <gracz> - podsumowanie: serwer, ping, IP, UUID, status bana i wyciszenia w jednym miejscu. */
public class WhoisCommand extends Command implements TabExecutor {

    private static final String PERMISSION = "banmanager.whois";

    private final BanManagerPlugin plugin;

    public WhoisCommand(BanManagerPlugin plugin) {
        super("whois", null);
        this.plugin = plugin;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (!Permissions.has(sender, PERMISSION)) {
            sender.sendMessage(TextComponent.fromLegacyText("§cNie masz uprawnień do tej komendy."));
            return;
        }
        if (args.length == 0) {
            sender.sendMessage(TextComponent.fromLegacyText("§cUżycie: /whois <gracz>"));
            return;
        }
        ProxiedPlayer target = ProxyServer.getInstance().getPlayer(args[0]);
        if (target == null) {
            sender.sendMessage(TextComponent.fromLegacyText("§cGracz '" + args[0] + "' nie jest online."));
            return;
        }
        String server = target.getServer() != null ? target.getServer().getInfo().getName() : "brak";
        String ip = MenuHelper.ipOf(target);
        BanEntry ban = plugin.getStorage().findBlocking(BanEntry.Type.PLAYER, target.getName().toLowerCase(), server);
        MuteManager.MuteEntry mute = plugin.getMutes().getMute(target.getName().toLowerCase());

        sender.sendMessage(TextComponent.fromLegacyText("§8§m----§r §e§l" + target.getName() + " §8§m----"));
        sender.sendMessage(TextComponent.fromLegacyText("§7Serwer: §f" + server + " §7| Ping: §f" + target.getPing() + "ms"));
        sender.sendMessage(TextComponent.fromLegacyText("§7IP: §f" + (ip != null ? ip : "nieznane")));
        sender.sendMessage(TextComponent.fromLegacyText("§7UUID: §f" + target.getUniqueId()));
        sender.sendMessage(TextComponent.fromLegacyText(ban != null
                ? "§7Ban: §c" + ban.getScope() + " §7- §f" + DurationParser.format(ban.getExpiresAt())
                : "§7Ban: §aBrak"));
        sender.sendMessage(TextComponent.fromLegacyText(mute != null
                ? "§7Wyciszenie: §c" + DurationParser.format(mute.getExpiresAt())
                : "§7Wyciszenie: §aBrak"));
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
