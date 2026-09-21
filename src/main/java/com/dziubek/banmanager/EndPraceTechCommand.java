package com.dziubek.banmanager;

import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.plugin.Command;

/**
 * /endpracetech (alias /koniecpracetech) - awaryjny "duży czerwony przycisk": bez argumentów
 * kończy WSZYSTKIE prace techniczne/zamknięcia na WSZYSTKICH serwerach naraz i kasuje wszystkie
 * trwające odliczenia /shutdown, /pracetech. W odróżnieniu od /startserver (które działa na
 * podanym zasięgu i jeśli zamknięcie było przez "all", zdjęcie pojedynczego serwera nic nie da),
 * ta komenda zawsze wszystko otwiera - na wypadek gdy admin nie pamięta jak dokładnie zamknął.
 */
public class EndPraceTechCommand extends Command {

    private static final String PERMISSION = "banmanager.shutdown";

    private final ShutdownCommand shutdownCommand;

    public EndPraceTechCommand(ShutdownCommand shutdownCommand) {
        super("endpracetech", null, "koniecpracetech");
        this.shutdownCommand = shutdownCommand;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (!Permissions.has(sender, PERMISSION)) {
            sender.sendMessage(TextComponent.fromLegacyText("§cNie masz uprawnień do tej komendy."));
            return;
        }
        shutdownCommand.forceEndAll(sender);
    }
}
