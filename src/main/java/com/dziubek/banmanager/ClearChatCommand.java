package com.dziubek.banmanager;

import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.TabExecutor;

import java.util.ArrayList;
import java.util.List;

/** /clearchat [serwer] - "czyści" czat pustymi liniami (kosmetyczne - nie usuwa niczego z logów serwera). */
public class ClearChatCommand extends Command implements TabExecutor {

    private static final String PERMISSION = "banmanager.clearchat";
    private static final int LINES = 100;

    public ClearChatCommand() {
        super("clearchat", null);
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (!Permissions.has(sender, PERMISSION)) {
            sender.sendMessage(TextComponent.fromLegacyText("§cNie masz uprawnień do tej komendy."));
            return;
        }
        if (args.length == 0) {
            for (ProxiedPlayer player : ProxyServer.getInstance().getPlayers()) {
                clear(player);
            }
            sender.sendMessage(TextComponent.fromLegacyText("§aWyczyszczono czat wszystkim."));
            return;
        }
        ServerInfo info = ProxyServer.getInstance().getServerInfo(args[0]);
        if (info == null) {
            sender.sendMessage(TextComponent.fromLegacyText("§cNieznany serwer '" + args[0] + "'."));
            return;
        }
        for (ProxiedPlayer player : info.getPlayers()) {
            clear(player);
        }
        sender.sendMessage(TextComponent.fromLegacyText("§aWyczyszczono czat na §f" + info.getName() + "§a."));
    }

    private void clear(ProxiedPlayer player) {
        for (int i = 0; i < LINES; i++) {
            player.sendMessage(TextComponent.fromLegacyText(" "));
        }
    }

    @Override
    public Iterable<String> onTabComplete(CommandSender sender, String[] args) {
        List<String> options = new ArrayList<>();
        if (args.length <= 1) {
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
