package com.example.uninaswapoobd2425.model;

import java.time.LocalDateTime;

public class recensione {
    private int idRecensione;
    private int idTransazione;
    private int idAnnuncio;
    private String titoloAnnuncio;
    private transazione transazione;
    private utente recensore;
    private utente recensito;
    private String matricolaRecensore;
    private String matricolaRecensito;
    private int voto;
    private String commento;
    private LocalDateTime dataRecensione;

    public int getIdRecensione() { return idRecensione; }
    public int getIdTransazione() { return idTransazione; }
    public int getIdAnnuncio() { return idAnnuncio; }
    public String getTitoloAnnuncio() { return titoloAnnuncio; }
    public transazione getTransazione() { return transazione; }
    public utente getRecensore() { return recensore; }
    public utente getRecensito() { return recensito; }
    public String getMatricolaRecensore() { return matricolaRecensore; }
    public String getMatricolaRecensito() { return matricolaRecensito; }
    public int getVoto() { return voto; }
    public String getCommento() { return commento; }
    public LocalDateTime getDataRecensione() { return dataRecensione; }

    public void setIdRecensione(int idRecensione) { this.idRecensione = idRecensione; }
    public void setIdTransazione(int idTransazione) { this.idTransazione = idTransazione; }
    public void setIdAnnuncio(int idAnnuncio) { this.idAnnuncio = idAnnuncio; }
    public void setTitoloAnnuncio(String titoloAnnuncio) { this.titoloAnnuncio = titoloAnnuncio; }
    public void setTransazione(transazione transazione) { this.transazione = transazione; }
    public void setRecensore(utente recensore) { this.recensore = recensore; }
    public void setRecensito(utente recensito) { this.recensito = recensito; }
    public void setMatricolaRecensore(String matricolaRecensore) { this.matricolaRecensore = matricolaRecensore; }
    public void setMatricolaRecensito(String matricolaRecensito) { this.matricolaRecensito = matricolaRecensito; }
    public void setVoto(int voto) { this.voto = voto; }
    public void setCommento(String commento) { this.commento = commento; }
    public void setDataRecensione(LocalDateTime dataRecensione) { this.dataRecensione = dataRecensione; }
}
