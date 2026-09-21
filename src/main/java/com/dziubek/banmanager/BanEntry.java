package com.dziubek.banmanager;

/** Jeden aktywny ban (gracza albo IP), globalny albo ograniczony do jednego serwera z proxy. */
public class BanEntry {

    public enum Type {
        PLAYER, IP
    }

    private final int id;
    private final Type type;
    private final String target;   // gracz: lowercase nick. IP: adres tak jak podany.
    private final String display;  // nazwa do pokazania (oryginalna wielkosc liter / "Nick (IP)")
    private final String scope;    // "GLOBAL" albo dokladna nazwa serwera z proxy
    private final String reason;
    private final String by;
    private final long createdAt;
    private final long expiresAt;  // -1 = na zawsze

    public BanEntry(int id, Type type, String target, String display, String scope,
                     String reason, String by, long createdAt, long expiresAt) {
        this.id = id;
        this.type = type;
        this.target = target;
        this.display = display;
        this.scope = scope;
        this.reason = reason;
        this.by = by;
        this.createdAt = createdAt;
        this.expiresAt = expiresAt;
    }

    public int getId() {
        return id;
    }

    public Type getType() {
        return type;
    }

    public String getTarget() {
        return target;
    }

    public String getDisplay() {
        return display;
    }

    public String getScope() {
        return scope;
    }

    public String getReason() {
        return reason;
    }

    public String getBy() {
        return by;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public long getExpiresAt() {
        return expiresAt;
    }

    public boolean isPermanent() {
        return expiresAt < 0;
    }

    public boolean isExpired() {
        return !isPermanent() && System.currentTimeMillis() >= expiresAt;
    }

    public boolean isGlobal() {
        return scope.equalsIgnoreCase("GLOBAL");
    }
}
