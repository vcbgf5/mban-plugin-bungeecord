package com.dziubek.banmanager;

import net.md_5.bungee.api.CommandSender;

/**
 * LuckPerms "admin.wholeserver" (blankietowe uprawnienie admina całej sieci) zawsze daje dostęp
 * do WSZYSTKICH komend tego pluginu, niezależnie od granularnych węzłów banmanager.* - nie trzeba
 * nadawać każdego z osobna w LuckPerms, jeśli ktoś ma już to jedno uprawnienie administratora.
 * Konsola zawsze ma dostęp (Command#hasPermission traktuje konsolę jako uprawnioną do wszystkiego,
 * ale sprawdzamy jawnie, żeby nie polegać na tym niejawnie).
 */
public final class Permissions {

    public static final String ADMIN_WHOLE_SERVER = "admin.wholeserver";

    private Permissions() {
    }

    public static boolean has(CommandSender sender, String specificPermission) {
        return sender.hasPermission(ADMIN_WHOLE_SERVER) || sender.hasPermission(specificPermission);
    }
}
