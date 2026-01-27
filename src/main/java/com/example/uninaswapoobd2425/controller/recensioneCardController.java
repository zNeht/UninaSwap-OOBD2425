package com.example.uninaswapoobd2425.controller;

import com.example.uninaswapoobd2425.dao.recensioniDAO;
import javafx.fxml.FXML;
import javafx.scene.control.Label;

public class recensioneCardController {
    @FXML private Label lblTitolo;
    @FXML private Label star1, star2, star3, star4, star5;
    @FXML private Label lblCommento;
    @FXML private Label lblUtente;
    @FXML private Label lblUtenteTitle;
    @FXML private Label lblData;

    public void setData(recensioniDAO.RecensioneView v, boolean ricevuta) {
        lblTitolo.setText(v.titoloAnnuncio != null ? v.titoloAnnuncio : "Annuncio");

        if (v.commento != null && !v.commento.isBlank()) {
            lblCommento.setText(v.commento.trim());
        } else {
            lblCommento.setText("(Nessun commento)");
        }
        // Se la card è lato venditore (ricevuta=true) mostra chi ha recensito, altrimenti mostra il venditore recensito
        String utenteLabel = ricevuta ? (v.recensore != null ? v.recensore : "-") : (v.recensito != null ? v.recensito : "-");
        lblUtente.setText(utenteLabel);
        lblUtenteTitle.setText(ricevuta ? "Recensitore" : "Venditore");
        if (v.data != null) {
            lblData.setText(v.data.toLocalDate().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy")));
        } else {
            lblData.setText("-");
        }
        paintStars(v.voto);
    }

    private void paintStars(int voto) {
        Label[] stars = {star1, star2, star3, star4, star5};
        for (int i = 0; i < stars.length; i++) {
            boolean on = voto >= i + 1;
            stars[i].setText(on ? "★" : "☆");
            stars[i].setStyle("-fx-text-fill: " + (on ? "#E8CD9A" : "#9E9E9E") + "; -fx-font-size: 16px; -fx-font-weight: bold;");
        }
    }
}
