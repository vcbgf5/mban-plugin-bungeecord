package com.dziubek.banmanager;

import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.TabExecutor;

import java.util.ArrayList;
import java.util.List;

/** /munbanip <IP> [global|serwer] - dziala tak samo jak /munban, ale dla adresow IP. */
public class UnbanIpCommand extends Command implements TabExecutor {

    private static final String PERMISSION = "banmanager.unban";

    private final BanManagerPlugin plugin;

    public UnbanIpCommand(BanManagerPlugin plugin) {
        super("munbanip", null);
        this.plugin = plugin;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (!Permissions.has(sender, PERMISSION)) {
            sender.sendMessage(TextComponent.fromLegacyText("§cNie masz uprawnień do tej komendy."));
            return;
        }
        if (args.length == 0) {
            sender.sendMessage(TextComponent.fromLegacyText("§cUzycie: /munbanip <IP> [global|serwer]"));
            return;
        }
        String ip = args[0];
        List<BanEntry> active = plugin.getStorage().findActive(BanEntry.Type.IP, ip);
        if (active.isEmpty()) {
            sender.sendMessage(TextComponent.fromLegacyText("§cIP '" + ip + "' nie ma zadnego aktywnego bana."));
            return;
        }

        if (args.length >= 2) {
            String scope = args[1].equalsIgnoreCase("global") ? "GLOBAL" : args[1];
            remove(sender, ip, scope);
            return;
        }

        if (active.size() == 1) {
            remove(sender, ip, active.get(0).getScope());
            return;
        }

        if (!(sender instanceof ProxiedPlayer)) {
            StringBuilder sb = new StringBuilder("§eTo IP ma kilka banow, podaj ktory zdjac: ");
            for (BanEntry entry : active) {
                sb.append(entry.getScope()).append(" ");
            }
            sender.sendMessage(TextComponent.fromLegacyText(sb.toString()));
            return;
        }

        ChatMenuUtil.sendHeader((ProxiedPlayer) sender, "&eTo IP ma kilka banow - wybierz ktory zdjac");
        for (BanEntry entry : active) {
            ChatMenuUtil.sendButton((ProxiedPlayer) sender, "&c[" + entry.getScope() + "]",
                    "&7Powod: &f" + entry.getReason() + "\n&7Przez: &f" + entry.getBy(),
                    "/munbanip " + ip + " " + entry.getScope());
        }
    }

    private void remove(CommandSender sender, String ip, String scope) {
        boolean removed = plugin.getStorage().removeBan(BanEntry.Type.IP, ip, scope);
        sender.sendMessage(TextComponent.fromLegacyText(removed
                ? "§aZdjeto ban z IP §f" + ip + " §7[" + scope + "]"
                : "§cIP '" + ip + "' nie ma bana o zasiegu '" + scope + "'."));
    }

    @Override
    public Iterable<String> onTabComplete(CommandSender sender, String[] args) {
        List<String> options = new ArrayList<>();
        if (args.length <= 1) {
            for (BanEntry entry : plugin.getStorage().list(null, null)) {
                if (entry.getType() == BanEntry.Type.IP) {
                    options.add(entry.getTarget());
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
