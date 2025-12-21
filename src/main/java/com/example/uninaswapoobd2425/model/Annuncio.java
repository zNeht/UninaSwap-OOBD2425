package com.example.uninaswapoobd2425.model;

public class Annuncio {
    private String titolo;
    private String prezzo; // Stringa per gestire anche "Scambio" o "Regalo"
    private String immaginePath; // Nome del file immagine

    public Annuncio(String titolo, String prezzo, String immaginePath) {
        this.titolo = titolo;
        this.prezzo = prezzo;
        this.immaginePath = immaginePath;
    }

    public String getTitolo() { return titolo; }
    public String getPrezzo() { return prezzo; }
    public String getImmaginePath() { return immaginePath; }
}
