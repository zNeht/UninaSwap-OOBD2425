package com.example.uninaswapoobd2425.model;

import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;

public class annuncioCard extends VBox {
    private annuncio annuncio;

    public annuncioCard(annuncio annuncio) {
        this.annuncio = annuncio;

        setSpacing(5);
        setPadding(new Insets(10));
        setStyle("-fx-background-color: white; -fx-border-color: #ccc; -fx-border-radius: 5; -fx-background-radius: 5;");

        // Immagine
        ImageView img = new ImageView(new Image(annuncio.getImmaginePath()));
        img.setFitWidth(100);
        img.setFitHeight(100);

        // Titolo
        Label titolo = new Label(annuncio.getTitolo());
        titolo.setStyle("-fx-font-weight: bold; -fx-font-size: 14;");

        // Prezzo o tipo
        Label prezzo = new Label(annuncio.getTipo() == tipoAnnuncio.vendita ?
                "€ " + annuncio.getPrezzo() : annuncio.getTipo().toString());

        // Bottone dettagli
        Button btn = new Button("Visualizza");
        btn.setOnAction(e -> openItemPage(annuncio));

        getChildren().addAll(img, titolo, prezzo, btn);
    }

    private void openItemPage(annuncio a) {
        // Qui apri item.fxml con dettagli annuncio

    }
}