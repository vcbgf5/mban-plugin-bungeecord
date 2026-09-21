package com.dziubek.banmanager;

import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.TabExecutor;

import java.util.ArrayList;
import java.util.List;

/**
 * /startserver <serwer|all> - czytelniejszy odpowiednik "/shutdown cancel": otwiera z powrotem
 * serwer zamknięty przez /shutdown (i przerywa odliczanie, jeśli akurat trwa). Deleguje do
 * ShutdownCommand, żeby nie duplikować stanu aktywnych odliczeń.
 */
public class StartServerCommand extends Command implements TabExecutor {

    private static final String PERMISSION = "banmanager.shutdown";

    private final ShutdownCommand shutdownCommand;

    public StartServerCommand(ShutdownCommand shutdownCommand) {
        super("startserver", null);
        this.shutdownCommand = shutdownCommand;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (!Permissions.has(sender, PERMISSION)) {
            sender.sendMessage(TextComponent.fromLegacyText("§cNie masz uprawnień do tej komendy."));
            return;
        }
        if (args.length == 0) {
            sender.sendMessage(TextComponent.fromLegacyText("§cUżycie: /startserver <serwer|all>"));
            return;
        }
        shutdownCommand.cancel(sender, args[0]);
    }

    @Override
    public Iterable<String> onTabComplete(CommandSender sender, String[] args) {
        List<String> options = new ArrayList<>();
        if (args.length <= 1) {
            options.add("all");
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
