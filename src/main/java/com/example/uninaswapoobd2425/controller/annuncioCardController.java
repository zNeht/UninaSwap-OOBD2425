package com.example.uninaswapoobd2425.controller;

import com.example.uninaswapoobd2425.model.annuncio;
import com.example.uninaswapoobd2425.model.tipoAnnuncio;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import java.io.File;


public class annuncioCardController {

    @FXML private ImageView img;
    @FXML private Label badgeTipo;
    @FXML private Label titolo;
    @FXML private Label prezzo;
    @FXML private Label categoria;

    public void setData(annuncio a) {
        titolo.setText(a.getTitolo());
        categoria.setText(capitalize(a.getCategoria().name()));

        tipoAnnuncio t = a.getTipo();

        switch (t) {
            case vendita -> {
                badgeTipo.setText("In vendita");
                prezzo.setText(a.getPrezzo() != null ? "€ " + a.getPrezzo() : "€ --");
            }
            case scambio -> {
                badgeTipo.setText("Scambio");
                prezzo.setText("Scambio");
            }
            case regalo -> {
                badgeTipo.setText("Regalo");
                prezzo.setText("Regalo");
            }
        }

        if (a.getImmaginePath() != null && !a.getImmaginePath().isBlank()) {
            File f = new File(System.getProperty("user.dir"), a.getImmaginePath());
            if (f.exists()) {
                img.setImage(new Image(f.toURI().toString(), true));
            }
        }
    }

    private String capitalize(String s) {
        if (s == null || s.isBlank()) return "";
        String t = s.trim();
        return t.substring(0, 1).toUpperCase() + t.substring(1);
    }
}
