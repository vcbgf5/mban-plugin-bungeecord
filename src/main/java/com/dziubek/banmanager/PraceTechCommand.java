package com.dziubek.banmanager;

import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.TabExecutor;

import java.util.ArrayList;
import java.util.List;

/**
 * /pracetech <serwer|all> <sekundy> - skrót na /shutdown z gotowym powodem "Prace techniczne",
 * żeby nie trzeba było go za każdym razem wpisywać ręcznie. Deleguje wprost do ShutdownCommand.
 */
public class PraceTechCommand extends Command implements TabExecutor {

    private static final String PERMISSION = "banmanager.shutdown";

    private final ShutdownCommand shutdownCommand;

    public PraceTechCommand(ShutdownCommand shutdownCommand) {
        super("pracetech", null);
        this.shutdownCommand = shutdownCommand;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (!Permissions.has(sender, PERMISSION)) {
            sender.sendMessage(TextComponent.fromLegacyText("§cNie masz uprawnień do tej komendy."));
            return;
        }
        if (args.length < 2) {
            sender.sendMessage(TextComponent.fromLegacyText("§cUżycie: /pracetech <serwer|all> <sekundy>"));
            return;
        }
        shutdownCommand.execute(sender, new String[] {args[0], args[1], "Prace", "techniczne"});
    }

    @Override
    public Iterable<String> onTabComplete(CommandSender sender, String[] args) {
        List<String> options = new ArrayList<>();
        if (args.length <= 1) {
            options.add("all");
            options.addAll(ProxyServer.getInstance().getServers().keySet());
        } else if (args.length == 2) {
            options.add("60");
            options.add("120");
            options.add("300");
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
