package com.dziubek.banmanager;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Parsuje czas trwania bana: liczba + jednostka (s/m/h/d/w), np. "30m", "1d", "2w", albo "perm"/"permanent"/"trwale" na stale. */
public final class DurationParser {

    private static final Pattern PATTERN = Pattern.compile("^(\\d+)([smhdw])$", Pattern.CASE_INSENSITIVE);

    private DurationParser() {
    }

    /** Zwraca millis do dodania do "teraz", albo -1 dla permanentnego bana. Rzuca IllegalArgumentException gdy format zly. */
    public static long parse(String raw) {
        String lower = raw.toLowerCase();
        if (lower.equals("perm") || lower.equals("permanent") || lower.equals("trwale") || lower.equals("-1")) {
            return -1L;
        }
        Matcher matcher = PATTERN.matcher(lower);
        if (!matcher.matches()) {
            throw new IllegalArgumentException("Zly format czasu: " + raw);
        }
        long amount = Long.parseLong(matcher.group(1));
        char unit = matcher.group(2).charAt(0);
        long unitMillis = switch (unit) {
            case 's' -> 1000L;
            case 'm' -> 60_000L;
            case 'h' -> 3_600_000L;
            case 'd' -> 86_400_000L;
            case 'w' -> 604_800_000L;
            default -> throw new IllegalArgumentException("Zly format czasu: " + raw);
        };
        return amount * unitMillis;
    }

    /** Czytelny opis: "na zawsze" dla permanentnego, albo pozostaly czas (np. "6d 3h") dla czasowego. */
    public static String format(long expiresAt) {
        if (expiresAt < 0) {
            return "na zawsze";
        }
        long remaining = expiresAt - System.currentTimeMillis();
        if (remaining <= 0) {
            return "wygasl";
        }
        long days = remaining / 86_400_000L;
        long hours = (remaining % 86_400_000L) / 3_600_000L;
        long minutes = (remaining % 3_600_000L) / 60_000L;

        StringBuilder sb = new StringBuilder();
        if (days > 0) {
            sb.append(days).append("d ");
        }
        if (hours > 0) {
            sb.append(hours).append("h ");
        }
        if (days == 0 && minutes > 0) {
            sb.append(minutes).append("min");
        }
        String result = sb.toString().trim();
        return result.isEmpty() ? "< 1min" : result;
    }
}
