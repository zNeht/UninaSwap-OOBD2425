package com.example.uninaswapoobd2425.model;

public final class Session {
    private static String matricola;

    private Session() {}

    public static String getMatricola() {
        return matricola;
    }

    public static void setMatricola(String value) {
        matricola = value;
    }

    public static void clear() {
        matricola = null;
    }
}
