package com.example.uninaswapoobd2425.model;

public class offerta {
    int idOfferta;
    annuncio annuncio;
    utente offerente;
    statoOfferta stato; // enum: IN_ATTESA, ACCETTATA, RIFIUTATA, RITIRATA
    double importoProposto;
    String messaggio;
}
