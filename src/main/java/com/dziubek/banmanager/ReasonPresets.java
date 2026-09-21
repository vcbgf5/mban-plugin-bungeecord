package com.dziubek.banmanager;

import java.util.List;

/** Gotowe powody bana pokazywane jako klikalne przyciski (z podpowiedzia) w /mban i /mbanip bez podanego powodu. */
public final class ReasonPresets {

    public static final List<String> REASONS = List.of(
            "Cheaty/Hacki",
            "Griefing",
            "Toksyczne zachowanie",
            "Reklama/Spam",
            "Oszustwo (scam)",
            "Obrazliwy nick/skin",
            "Obchodzenie bana (alt)"
    );

    private ReasonPresets() {
    }
}
