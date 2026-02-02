package com.example.uninaswapoobd2425.controller;

import com.example.uninaswapoobd2425.dao.DB;
import com.example.uninaswapoobd2425.dao.recensioniDAO;
import com.example.uninaswapoobd2425.model.Session;
import com.example.uninaswapoobd2425.model.recensione;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.TilePane;

import java.sql.Connection;
import java.util.List;

public class recensioniController {
    @FXML private TilePane tileRicevute;
    @FXML private TilePane tileInviate;
    @FXML private Button btnToggleRicevute;
    @FXML private Button btnToggleInviate;
    @FXML private Label lblMedia;
    @FXML private Label lblTotale;
    @FXML private Label starA, starB, starC, starD, starE;

    // Carica recensioni ricevute/inviate e aggiorna media.
    public void loadData() {
        String matricola = Session.getMatricola();
        if (matricola == null || matricola.isBlank()) return;

        try (Connection conn = DB.getConnection()) {
            recensioniDAO dao = new recensioniDAO(conn);
            List<recensione> ricevute = dao.getRicevute(matricola);
            List<recensione> inviate = dao.getInviate(matricola);
            render(tileRicevute, ricevute, true);
            render(tileInviate, inviate, false);
            updateMedia(ricevute);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    // Renderizza le recensioni in un TilePane.
    private void render(TilePane pane, List<recensione> list, boolean ricevute) throws Exception {
        pane.getChildren().clear();
        for (recensione v : list) {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(
                    "/com/example/uninaswapoobd2425/recensioneCard.fxml"
            ));
            Node card = loader.load();
            recensioneCardController c = loader.getController();
            c.setData(v, ricevute);
            pane.getChildren().add(card);
        }
    }

    @FXML
    // Toggle visibilita' recensioni ricevute.
    private void toggleRicevute() { togglePane(tileRicevute, btnToggleRicevute); }
    @FXML
    // Toggle visibilita' recensioni inviate.
    private void toggleInviate() { togglePane(tileInviate, btnToggleInviate); }

    // Utility: mostra/nasconde un TilePane e aggiorna il bottone.
    private void togglePane(TilePane pane, Button btn) {
        boolean newVis = !pane.isVisible();
        pane.setVisible(newVis);
        pane.setManaged(newVis);
        if (btn != null) btn.setText(newVis ? "▾" : "▸");
    }

    // Calcola la media voti e aggiorna il riepilogo.
    private void updateMedia(List<recensione> ricevute) {
        if (lblMedia == null || lblTotale == null) return;
        if (ricevute == null || ricevute.isEmpty()) {
            lblMedia.setText("0.0");
            lblTotale.setText("0 recensioni");
            paintStars(0);
            return;
        }
        double avg = ricevute.stream().mapToInt(recensione::getVoto).average().orElse(0);
        lblMedia.setText(String.format("%.1f", avg));
        lblTotale.setText(ricevute.size() + " recensioni");
        paintStars(avg);
    }

    // Colora le stelle in base al valore medio.
    private void paintStars(double value) {
        if (starA == null) return;
        Label[] stars = {starA, starB, starC, starD, starE};
        for (int i = 0; i < stars.length; i++) {
            boolean on = value >= i + 1 - 0.01;
            stars[i].setText(on ? "★" : "☆");
            stars[i].setStyle("-fx-text-fill: " + (on ? "#E8CD9A" : "#9E9E9E") + "; -fx-font-size: 16px; -fx-font-weight: bold;");
        }
    }
}
