package com.example.uninaswapoobd2425.controller;

import com.example.uninaswapoobd2425.model.annuncio;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;

public class offerScambioController implements offerPaneController {

    @FXML private ListView<String> listOggetti;
    @FXML private TextField txtDettagli;

    private annuncio ann;

    @Override
    public void init(annuncio a) {
        this.ann = a;
        listOggetti.setItems(FXCollections.observableArrayList());
    }

    @FXML
    private void handleAdd() {
        TextInputDialog d = new TextInputDialog();
        d.setTitle("Aggiungi oggetto");
        d.setHeaderText(null);
        d.setContentText("Nome oggetto:");
        d.showAndWait().ifPresent(name -> {
            if (!name.isBlank()) listOggetti.getItems().add(name.trim());
        });
    }

    @FXML
    private void handleInvia() {
        if (listOggetti.getItems().isEmpty()) {
            new Alert(Alert.AlertType.WARNING, "Inserisci almeno un oggetto da offrire.").showAndWait();
            return;
        }
        // TODO: salva offerta + oggetti_scambio nel DB
        new Alert(Alert.AlertType.INFORMATION, "Proposta di scambio inviata!").showAndWait();
    }
}
