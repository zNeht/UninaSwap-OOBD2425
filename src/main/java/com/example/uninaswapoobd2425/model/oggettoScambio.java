package com.example.uninaswapoobd2425.model;

public class oggettoScambio {
    private int idOggetto;
    private String nomeOggetto;
    private String path;
    private offerta offerta;

    public int getIdOggetto() {
        return idOggetto;
    }

    public void setIdOggetto(int idOggetto) {
        this.idOggetto = idOggetto;
    }

    public String getNomeOggetto() {
        return nomeOggetto;
    }

    public void setNomeOggetto(String nomeOggetto) {
        this.nomeOggetto = nomeOggetto;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public offerta getOfferta() {
        return offerta;
    }

    public void setOfferta(offerta offerta) {
        this.offerta = offerta;
    }
}
