package com.example.uninaswapoobd2425.model;

import java.util.prefs.Preferences;

public final class Session {
    private static final String KEY_MATRICOLA = "matricola";
    private static final Preferences PREFS = Preferences.userNodeForPackage(Session.class);

    private static String matricola = PREFS.get(KEY_MATRICOLA, null);

    private Session() {}

    public static String getMatricola() {
        return matricola;
    }

    public static void setMatricola(String value) {
        matricola = value;
        if (value == null || value.isBlank()) {
            PREFS.remove(KEY_MATRICOLA);
        } else {
            PREFS.put(KEY_MATRICOLA, value);
        }
    }

    public static void clear() {
        setMatricola(null);
    }
}
