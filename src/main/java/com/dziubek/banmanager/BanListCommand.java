package com.dziubek.banmanager;

import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.TabExecutor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/** /mbanlist [global|serwer] [search <fraza>] - lista aktywnych banow, filtrowalna po zasiegu i tekstem. */
public class BanListCommand extends Command implements TabExecutor {

    private static final int PAGE_SIZE = 15;
    private static final String PERMISSION = "banmanager.banlist";

    private final BanManagerPlugin plugin;

    public BanListCommand(BanManagerPlugin plugin) {
        super("mbanlist", null);
        this.plugin = plugin;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (!Permissions.has(sender, PERMISSION)) {
            sender.sendMessage(TextComponent.fromLegacyText("§cNie masz uprawnień do tej komendy."));
            return;
        }
        String scope = null;
        String search = null;

        int i = 0;
        if (args.length > i && !args[i].equalsIgnoreCase("search")) {
            scope = args[i].equalsIgnoreCase("global") ? "GLOBAL" : args[i];
            i++;
        }
        if (args.length > i && args[i].equalsIgnoreCase("search")) {
            i++;
            if (args.length > i) {
                search = String.join(" ", Arrays.copyOfRange(args, i, args.length));
            }
        }

        List<BanEntry> results = plugin.getStorage().list(scope, search);
        if (results.isEmpty()) {
            sender.sendMessage(TextComponent.fromLegacyText("§eBrak banow spelniajacych kryteria."));
            return;
        }

        sender.sendMessage(TextComponent.fromLegacyText("§8§m----§r §e§lBany" + (scope != null ? " [" + scope + "]" : "")
                + (search != null ? " szukane: '" + search + "'" : "") + " §8§m---- §7(" + results.size() + ")"));

        int shown = Math.min(results.size(), PAGE_SIZE);
        for (int idx = 0; idx < shown; idx++) {
            BanEntry entry = results.get(idx);
            String type = entry.getType() == BanEntry.Type.IP ? "§8[IP]" : "§8[Gracz]";
            sender.sendMessage(TextComponent.fromLegacyText(type + " §f" + entry.getDisplay() + " §7[" + entry.getScope()
                    + "] §7- §f" + DurationParser.format(entry.getExpiresAt()) + " §7- §f" + entry.getReason()
                    + " §8(przez " + entry.getBy() + ")"));
        }
        if (results.size() > PAGE_SIZE) {
            sender.sendMessage(TextComponent.fromLegacyText("§7... i " + (results.size() - PAGE_SIZE)
                    + " wiecej. Zawez wyszukiwanie: /mbanlist [serwer] search <fraza>"));
        }
    }

    @Override
    public Iterable<String> onTabComplete(CommandSender sender, String[] args) {
        List<String> options = new ArrayList<>();
        if (args.length <= 1) {
            options.add("global");
            options.add("search");
            options.addAll(ProxyServer.getInstance().getServers().keySet());
        } else if (args.length == 2 && !args[0].equalsIgnoreCase("search")) {
            options.add("search");
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
