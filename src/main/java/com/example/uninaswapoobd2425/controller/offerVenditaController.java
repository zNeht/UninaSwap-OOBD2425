package com.example.uninaswapoobd2425.controller;

import com.example.uninaswapoobd2425.model.annuncio;
import javafx.fxml.FXML;
import javafx.scene.control.*;

public class offerVenditaController implements offerPaneController {

    @FXML private Label lblRichiesto;
    @FXML private TextField txtOfferta;
    @FXML private TextField txtNota;

    private annuncio ann;

    @Override
    public void init(annuncio a) {
        this.ann = a;
        lblRichiesto.setText("€ " + a.getPrezzo().toString());
    }

    @FXML
    private void handleAccetta() {
        txtOfferta.setText(ann.getPrezzo().toString());
        handleInvia();
    }

    @FXML
    private void handleInvia() {
        if (txtOfferta.getText().isBlank()) {
            new Alert(Alert.AlertType.WARNING, "Inserisci un prezzo.").showAndWait();
            return;
        }
        // TODO: salva offerta nel DB
        new Alert(Alert.AlertType.INFORMATION, "Offerta inviata!").showAndWait();
    }
}
