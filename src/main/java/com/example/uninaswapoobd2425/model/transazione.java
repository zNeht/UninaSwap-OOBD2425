package com.example.uninaswapoobd2425.model;

import java.time.LocalDateTime;

public class transazione {
    private int idTransazione;
    private offerta offertaAccettata;
    private annuncio annuncio;
    private LocalDateTime dataConclusione;

    public int getIdTransazione() { return idTransazione; }
    public offerta getOffertaAccettata() { return offertaAccettata; }
    public annuncio getAnnuncio() { return annuncio; }
    public LocalDateTime getDataConclusione() { return dataConclusione; }

    public void setIdTransazione(int idTransazione) { this.idTransazione = idTransazione; }
    public void setOffertaAccettata(offerta offertaAccettata) { this.offertaAccettata = offertaAccettata; }
    public void setAnnuncio(annuncio annuncio) { this.annuncio = annuncio; }
    public void setDataConclusione(LocalDateTime dataConclusione) { this.dataConclusione = dataConclusione; }
}
