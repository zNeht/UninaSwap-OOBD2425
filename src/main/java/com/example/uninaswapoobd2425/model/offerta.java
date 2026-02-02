package com.example.uninaswapoobd2425.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class offerta {
    private int idOfferta;
    private annuncio annuncio;
    private utente offerente;
    private statoOfferta stato; // enum: in_attesa, accettata, rifiutata, ritirata
    private BigDecimal importoProposto;
    private String messaggio;
    private LocalDateTime dataOfferta;

    public int getIdOfferta() { return idOfferta; }
    public annuncio getAnnuncio() { return annuncio; }
    public utente getOfferente() { return offerente; }
    public statoOfferta getStato() { return stato; }
    public BigDecimal getImportoProposto() { return importoProposto; }
    public String getMessaggio() { return messaggio; }
    public LocalDateTime getDataOfferta() { return dataOfferta; }

    public void setIdOfferta(int idOfferta) { this.idOfferta = idOfferta; }
    public void setAnnuncio(annuncio annuncio) { this.annuncio = annuncio; }
    public void setOfferente(utente offerente) { this.offerente = offerente; }
    public void setStato(statoOfferta stato) { this.stato = stato; }
    public void setImportoProposto(BigDecimal importoProposto) { this.importoProposto = importoProposto; }
    public void setMessaggio(String messaggio) { this.messaggio = messaggio; }
    public void setDataOfferta(LocalDateTime dataOfferta) { this.dataOfferta = dataOfferta; }
}
