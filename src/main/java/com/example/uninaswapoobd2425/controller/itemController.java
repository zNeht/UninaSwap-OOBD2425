package com.example.uninaswapoobd2425.controller;

import com.example.uninaswapoobd2425.model.Annuncio;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import java.util.Objects;

public class itemController {
    @FXML
    private Label lblTitolo;
    @FXML
    private Label lblPrezzo;
    @FXML
    private ImageView imgAnnuncio;

    public void setData(Annuncio annuncio) {
        lblTitolo.setText(annuncio.getTitolo());
        lblPrezzo.setText(annuncio.getPrezzo());

        // CARICAMENTO IMMAGINE SICURO
        // Assicurati di avere le immagini nella cartella src/main/resources/img/
        try {
            String path = "/img/" + annuncio.getImmaginePath();
            Image image = new Image(Objects.requireNonNull(getClass().getResourceAsStream(path)));
            imgAnnuncio.setImage(image);
        } catch (Exception e) {
            // Se l'immagine non si trova, non crashare ma lascia vuoto o metti un placeholder
            System.out.println("Immagine non trovata: " + annuncio.getImmaginePath());
        }
    }
}
