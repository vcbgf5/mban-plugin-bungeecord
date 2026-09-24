package com.dziubek.banmanager;

import net.md_5.bungee.api.Favicon;

import javax.imageio.ImageIO;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

/**
 * Ikona serwera wymuszana przez proxy na KAŻDYM pingu (ServerIconListener) - nadpisuje to, co gracz
 * zobaczyłby normalnie z domyślnego server-icon.png BungeeCorda. Admin po prostu wrzuca plik
 * "icon.png" do plugins/BanManager/servericon/ i wywołuje /reloadicon (albo restartuje proxy) -
 * nie trzeba ręcznie podmieniać pliku w katalogu głównym BungeeCorda.
 */
public class ServerIconManager {

    private static final String FILE_NAME = "icon.png";
    private static final int SIZE = 64;

    private final BanManagerPlugin plugin;
    private final File folder;
    private volatile Favicon favicon;

    public ServerIconManager(BanManagerPlugin plugin) {
        this.plugin = plugin;
        this.folder = new File(plugin.getDataFolder(), "servericon");
    }

    public void load() {
        if (!folder.exists()) {
            folder.mkdirs();
        }
        File iconFile = new File(folder, FILE_NAME);
        if (!iconFile.exists()) {
            favicon = null;
            plugin.getLogger().info("Brak wlasnej ikony serwera - wrzuc plik '" + FILE_NAME
                    + "' do " + folder.getPath() + " i wykonaj /reloadicon, zeby ja wymusic.");
            return;
        }
        try {
            BufferedImage image = ImageIO.read(iconFile);
            if (image == null) {
                throw new IOException("Nie rozpoznano formatu obrazu");
            }
            if (image.getWidth() != SIZE || image.getHeight() != SIZE) {
                image = resize(image, SIZE);
            }
            favicon = Favicon.create(image);
            plugin.getLogger().info("Wlasna ikona serwera wczytana z " + iconFile.getPath() + " - wymuszana na kazdym pingu.");
        } catch (IOException e) {
            favicon = null;
            plugin.getLogger().warning("Nie udalo sie wczytac ikony serwera (" + iconFile.getPath() + "): " + e.getMessage());
        }
    }

    public Favicon getFavicon() {
        return favicon;
    }

    private static BufferedImage resize(BufferedImage source, int size) {
        BufferedImage resized = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = resized.createGraphics();
        g.drawImage(source, 0, 0, size, size, null);
        g.dispose();
        return resized;
    }
}
