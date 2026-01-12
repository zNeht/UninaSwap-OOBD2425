package com.example.uninaswapoobd2425.controller;

import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

public class ImageHandler {

    public static String scegliImmagine(Stage stage, String cartellaDestinazione) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Seleziona immagine");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Immagini", "*.png", "*.jpg", "*.jpeg", "*.gif")
        );

        File fileSelezionato = fileChooser.showOpenDialog(stage);
        if (fileSelezionato != null) {
            try {

                Path destinazione = Path.of(cartellaDestinazione, fileSelezionato.getName());
                Files.copy(fileSelezionato.toPath(), destinazione, StandardCopyOption.REPLACE_EXISTING);
                return destinazione.toString();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return null;
    }
}