package com.example.uninaswapoobd2425.model;

import java.math.BigDecimal;

public class annuncio {

    private int idAnnuncio;
    private String titolo;
    private String descrizione;

    private categoriaAnnuncio categoria;
    private tipoAnnuncio tipo;
    private BigDecimal prezzo;

    private statoAnnuncio stato;
    private utente venditore;
    private String venditoreEmail;
    private String immaginePath;

    public annuncio() {}

    // GETTERS
    public int getIdAnnuncio() { return idAnnuncio; }
    public String getTitolo() { return titolo; }
    public String getDescrizione() { return descrizione; }

    public categoriaAnnuncio getCategoria() { return categoria; }
    public tipoAnnuncio getTipo() { return tipo; }
    public BigDecimal getPrezzo() { return prezzo; }
    public String getVenditoreEmail() { return venditoreEmail; }

    public statoAnnuncio getStato() { return stato; }
    public utente getVenditore() { return venditore; }

    public String getImmaginePath() { return immaginePath; }

    // SETTERS
    public void setIdAnnuncio(int idAnnuncio) { this.idAnnuncio = idAnnuncio; }
    public void setTitolo(String titolo) { this.titolo = titolo; }
    public void setDescrizione(String descrizione) { this.descrizione = descrizione; }
    public void setVenditoreEmail(String venditoreEmail) { this.venditoreEmail = venditoreEmail; }
    public void setCategoria(categoriaAnnuncio categoria) { this.categoria = categoria; }
    public void setTipo(tipoAnnuncio tipo) { this.tipo = tipo; }
    public void setPrezzo(BigDecimal prezzo) { this.prezzo = prezzo; }

    public void setStato(statoAnnuncio stato) { this.stato = stato; }
    public void setVenditore(utente venditore) { this.venditore = venditore; }

    public void setImmaginePath(String immaginePath) { this.immaginePath = immaginePath; }

    // UTILI (opzionali)
    public boolean isVendita() { return tipo == tipoAnnuncio.vendita; }
    public boolean isScambio() { return tipo == tipoAnnuncio.scambio; }
    public boolean isRegalo() { return tipo == tipoAnnuncio.regalo; }
}