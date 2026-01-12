package com.example.uninaswapoobd2425.model;

public class annuncio {
    private int idAnnuncio;
    private String titolo;
    private String descrizione;
    private String categoria;
    private tipoAnnuncio tipoAnnuncio; // enum: VENDITA, SCAMBIO, REGALO
    private double prezzo;
    private String modalitaConsegna;
    private statoAnnuncio stato; // enum: ATTIVO, VENDUTO, SCAMBIATO, REGALATO
    private utente venditore;
    private String immagineUrl;

    public annuncio() {

    }
    //GETTERS
    public int getIdAnnuncio() {return idAnnuncio;}
    public String getTitolo() {return titolo;}
    public String getDescrizione() {return descrizione;}
    public String getCategoria() {return categoria;}
    public tipoAnnuncio getTipoAnnuncio() {return tipoAnnuncio;}
    public double getPrezzo() { return prezzo;}
    public String getModalitaConsegna() {return modalitaConsegna;}
    public statoAnnuncio getStato() {return stato;}
    public utente getVenditore() {return venditore;}
    public String getImmagineUrl() {return immagineUrl;}


    //SETTERS
    public void setIdAnnuncio(int id) {this.idAnnuncio = id;}
    public void setTitolo(String titolo) {this.titolo = titolo;}
    public void setDescrizione(String descrizione) {this.descrizione = descrizione;}
    public void setCategoria(String categoria) {this.categoria = categoria;}
    public void setTipoAnnuncio(tipoAnnuncio tipoAnnuncio) {this.tipoAnnuncio = tipoAnnuncio;}
    public void setPrezzo(double prezzo) {this.prezzo = prezzo;}
    public void setModalitaConsegna(String modalitaConsegna) {this.modalitaConsegna = modalitaConsegna;}
    public void setStato(statoAnnuncio stato) {this.stato = stato;}
    public void setVenditore(utente venditore) {this.venditore = venditore;}
    public void setImmagineUrl(String immagineUrl) {this.immagineUrl = immagineUrl;}
}