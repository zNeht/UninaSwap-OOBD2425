package com.example.uninaswapoobd2425.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;

public class nuovoAnnuncioController {
    @FXML
    private Button annullaButton;
    @FXML
    void handleAnnulla(ActionEvent event) {
        // Trucco per nascondere il genitore (l'overlay) senza complicati passaggi di dati
        annullaButton.getScene().lookup("#modalOverlay").setVisible(false);

        // OPPURE, se vuoi farlo più pulito risalendo la gerarchia:
        // ((StackPane) annullaButton.getParent().getParent()).setVisible(false);
    }
}
