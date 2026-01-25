package com.example.uninaswapoobd2425.controller;

import com.example.uninaswapoobd2425.model.annuncio;
import javafx.fxml.FXML;
import javafx.scene.control.*;

public class offerRegaloController implements offerPaneController {

    @FXML private TextArea txtMsg;
    @FXML private Label lblCount;

    private annuncio ann;

    @Override
    public void init(annuncio a) {
        this.ann = a;
        txtMsg.textProperty().addListener((obs, o, n) -> {
            if (n.length() > 300) txtMsg.setText(n.substring(0, 300));
            lblCount.setText(txtMsg.getText().length() + "/300");
        });
    }

    @FXML
    private void handleInvia() {
        if (txtMsg.getText().trim().isEmpty()) {
            new Alert(Alert.AlertType.WARNING, "Inserisci un messaggio.").showAndWait();
            return;
        }
        // TODO: salva offerta regalo nel DB
        new Alert(Alert.AlertType.INFORMATION, "Richiesta inviata!").showAndWait();
    }
}
