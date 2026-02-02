package com.example.uninaswapoobd2425.controller;

import com.example.uninaswapoobd2425.dao.DB;
import com.example.uninaswapoobd2425.dao.wishlistDAO;
import com.example.uninaswapoobd2425.model.Session;
import com.example.uninaswapoobd2425.model.annuncio;
import com.example.uninaswapoobd2425.model.tipoAnnuncio;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.ToggleButton;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.TilePane;
import javafx.scene.input.MouseEvent;

import java.io.File;
import java.sql.Connection;


public class annuncioCardController {

    @FXML private ImageView img;
    @FXML private Label badgeTipo;
    @FXML private Label titolo;
    @FXML private Label prezzo;
    @FXML private Label categoria;
    @FXML private javafx.scene.layout.HBox wishlistPill;
    @FXML private ToggleButton btnWishlist;
    @FXML private Label lblWishlistCount;

    private annuncio ann;
    private boolean suppressWishlistEvent = false;
    private boolean removeOnUnfavorite = false;

    //Imposto l'icona wishlist vuota e count a 0 e blocco il click diretto sul bottone toggle
    @FXML
    private void initialize() {
        updateWishlistVisual(false);
        lblWishlistCount.setText("0");
        btnWishlist.addEventFilter(MouseEvent.MOUSE_CLICKED, MouseEvent::consume);
    }

    //Wrapper che chiama la versione completa di setData il dove removeOnUnFavorite è false
    public void setData(annuncio a, boolean wishlisted, int wishlistCount) {
        setData(a, wishlisted, wishlistCount, false);
    }

    //Riempie la card con titolo , categoria , prezzo ecc.. e lo stato della wishlist
    public void setData(annuncio a, boolean wishlisted, int wishlistCount, boolean removeOnUnfavorite) {
        this.ann = a;
        this.removeOnUnfavorite = removeOnUnfavorite;
        titolo.setText(a.getTitolo());
        categoria.setText(a.getCategoria() != null ? capitalize(a.getCategoria().name()) : "-");
        lblWishlistCount.setText(String.valueOf(wishlistCount));

        tipoAnnuncio t = a.getTipo();

        switch (t) {
            case vendita -> {
                badgeTipo.setText("In vendita");
                prezzo.setText(a.getPrezzo() != null ? "€ " + a.getPrezzo() : "€ --");
                prezzo.setVisible(true);
                prezzo.setManaged(true);
            }
            case scambio -> {
                badgeTipo.setText("Scambio");
                prezzo.setText("");
                prezzo.setVisible(false);
                prezzo.setManaged(false);
            }
            case regalo -> {
                badgeTipo.setText("Regalo");
                prezzo.setText("");
                prezzo.setVisible(false);
                prezzo.setManaged(false);
            }
        }

        suppressWishlistEvent = true;
        btnWishlist.setSelected(wishlisted);
        updateWishlistVisual(wishlisted);
        suppressWishlistEvent = false;

        if (a.getImmaginePath() != null && !a.getImmaginePath().isBlank()) {
            File f = new File(System.getProperty("user.dir"), a.getImmaginePath());
            if (f.exists()) {
                img.setImage(new Image(f.toURI().toString(), true));
            }
        }
    }

    //Gestisce la rimozione/aggiunta ai preferiti sul DB e se richiesto rimuove la card
    @FXML
    private void handleWishlist() {
        if (suppressWishlistEvent || ann == null) {
            return;
        }
        String matricola = Session.getMatricola();
        if (matricola == null || matricola.isBlank()) {
            new Alert(Alert.AlertType.WARNING, "Devi essere loggato per aggiungere ai preferiti.").showAndWait();
            revertWishlist();
            return;
        }

        boolean selected = btnWishlist.isSelected();
        try (Connection conn = DB.getConnection()) {
            wishlistDAO dao = new wishlistDAO(conn);
            if (selected) {
                dao.add(ann.getIdAnnuncio(), matricola);
            } else {
                dao.remove(ann.getIdAnnuncio(), matricola);
            }
        } catch (Exception ex) {
            new Alert(Alert.AlertType.ERROR, "Errore durante l'aggiornamento dei preferiti.").showAndWait();
            revertWishlist();
            ex.printStackTrace();
            return;
        }

        updateWishlistVisual(selected);
        int count = parseWishlistCount();
        int next = selected ? count + 1 : Math.max(0, count - 1);
        lblWishlistCount.setText(String.valueOf(next));

        if (!selected && removeOnUnfavorite) {
            removeFromParent();
        }
    }
    //Rende cliccabile tutta la pill invece di cliccare solo il cuore
    @FXML
    private void handleWishlistPill(MouseEvent event) {
        if (event != null) {
            event.consume();
        }
        if (event != null && event.getTarget() instanceof ToggleButton) {
            return;
        }
        btnWishlist.fire();
    }
    //Rollback se login mancante o errore DB
    private void revertWishlist() {
        suppressWishlistEvent = true;
        btnWishlist.setSelected(!btnWishlist.isSelected());
        updateWishlistVisual(btnWishlist.isSelected());
        suppressWishlistEvent = false;
    }

    // Aggiorna l'icona del cuore in base allo stato.
    private void updateWishlistVisual(boolean selected) {
        btnWishlist.setText(selected ? "♥" : "♡");
    }

    //Converte il numero della label in int
    private int parseWishlistCount() {
        try {
            return Integer.parseInt(lblWishlistCount.getText());
        } catch (NumberFormatException ex) {
            return 0;
        }
    }
    //Rimuove la card dal TilePane (Caso nella sezione preferiti)
    private void removeFromParent() {
        if (btnWishlist.getScene() == null) return;
        javafx.scene.Node node = btnWishlist;
        while (node != null && !(node.getParent() instanceof TilePane)) {
            node = node.getParent();
        }
        if (node != null && node.getParent() instanceof TilePane tile) {
            tile.getChildren().remove(node);
        }
    }

    // Capitalizza la prima lettera per la UI.
    private String capitalize(String s) {
        if (s == null || s.isBlank()) return "";
        String t = s.trim();
        return t.substring(0, 1).toUpperCase() + t.substring(1);
    }

}
