package com.example.uninaswapoobd2425.controller;

import com.example.uninaswapoobd2425.model.annuncio;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;

import java.io.File;
import java.util.Objects;

public class itemController {
    @FXML
    private Label lblTitolo;
    @FXML
    private Label lblPrezzo;
    @FXML
    private ImageView imgAnnuncio;

    @FXML
    private void caricaImmagine() {
        String percorso = ImageHandler.scegliImmagine(new Stage(), "src/main/resources/com/example/uninaswapoobd2425/imgs/imgAnnunci");
        if (percorso != null) {
            annuncio nuovoAnnuncio = new annuncio();
            nuovoAnnuncio.setImmagineUrl(percorso);
            System.out.println("Immagine salvata in: " + nuovoAnnuncio.getImmagineUrl());
        }
    }

    @FXML
    private ImageView imageViewAnnuncio;

    private void mostraImmagine(annuncio a) {
        if (a.getImmagineUrl() != null) {
            File file = new File(a.getImmagineUrl());
            imageViewAnnuncio.setImage(new javafx.scene.image.Image(file.toURI().toString()));
        }
    }
}
