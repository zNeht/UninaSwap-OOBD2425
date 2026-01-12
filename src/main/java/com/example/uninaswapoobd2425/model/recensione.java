package com.example.uninaswapoobd2425.model;

import java.time.LocalDateTime;

public class recensione {
    int idRecensione;
    transazione transazione;
    utente recensore;
    utente recensito;
    int voto;
    String commento;
    LocalDateTime dataRecensione;
}
