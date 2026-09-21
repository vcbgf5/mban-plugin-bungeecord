package com.dziubek.banmanager;

import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.TabExecutor;

import java.util.ArrayList;
import java.util.List;

/**
 * /munban <gracz> [global|serwer] - jesli gracz ma dokladnie jeden aktywny ban, zdejmuje go bez
 * pytania o zasieg; jesli ma kilka (np. globalny + na innym serwerze skad juz go zdjeto), pokazuje
 * klikalne menu do wyboru ktory zdjac.
 */
public class UnbanCommand extends Command implements TabExecutor {

    private final BanManagerPlugin plugin;

    public UnbanCommand(BanManagerPlugin plugin) {
        super("munban", "banmanager.unban");
        this.plugin = plugin;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (args.length == 0) {
            sender.sendMessage(TextComponent.fromLegacyText("§cUzycie: /munban <gracz> [global|serwer]"));
            return;
        }
        String target = args[0].toLowerCase();
        List<BanEntry> active = plugin.getStorage().findActive(BanEntry.Type.PLAYER, target);
        if (active.isEmpty()) {
            sender.sendMessage(TextComponent.fromLegacyText("§cGracz '" + args[0] + "' nie ma zadnego aktywnego bana."));
            return;
        }

        if (args.length >= 2) {
            String scope = args[1].equalsIgnoreCase("global") ? "GLOBAL" : args[1];
            remove(sender, target, scope, args[0]);
            return;
        }

        if (active.size() == 1) {
            remove(sender, target, active.get(0).getScope(), args[0]);
            return;
        }

        if (!(sender instanceof ProxiedPlayer)) {
            StringBuilder sb = new StringBuilder("§eTen gracz ma kilka banow, podaj ktory zdjac: ");
            for (BanEntry entry : active) {
                sb.append(entry.getScope()).append(" ");
            }
            sender.sendMessage(TextComponent.fromLegacyText(sb.toString()));
            return;
        }

        ChatMenuUtil.sendHeader((ProxiedPlayer) sender, "&eGracz ma kilka banow - wybierz ktory zdjac");
        for (BanEntry entry : active) {
            ChatMenuUtil.sendButton((ProxiedPlayer) sender, "&c[" + entry.getScope() + "]",
                    "&7Powod: &f" + entry.getReason() + "\n&7Przez: &f" + entry.getBy(),
                    "/munban " + args[0] + " " + entry.getScope());
        }
    }

    private void remove(CommandSender sender, String targetLower, String scope, String displayName) {
        boolean removed = plugin.getStorage().removeBan(BanEntry.Type.PLAYER, targetLower, scope);
        sender.sendMessage(TextComponent.fromLegacyText(removed
                ? "§aZdjeto ban graczowi §f" + displayName + " §7[" + scope + "]"
                : "§cGracz '" + displayName + "' nie ma bana o zasiegu '" + scope + "'."));
    }

    @Override
    public Iterable<String> onTabComplete(CommandSender sender, String[] args) {
        List<String> options = new ArrayList<>();
        if (args.length <= 1) {
            for (BanEntry entry : plugin.getStorage().list(null, null)) {
                if (entry.getType() == BanEntry.Type.PLAYER) {
                    options.add(entry.getDisplay());
                }
            }
        } else if (args.length == 2) {
            options.add("global");
            options.addAll(ProxyServer.getInstance().getServers().keySet());
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
