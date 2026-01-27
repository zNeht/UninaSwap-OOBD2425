package com.example.uninaswapoobd2425.controller;

import com.example.uninaswapoobd2425.dao.DB;
import com.example.uninaswapoobd2425.model.annuncio;
import com.example.uninaswapoobd2425.model.Session;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Types;

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
        String raw = txtOfferta.getText() == null ? "" : txtOfferta.getText().trim();
        if (raw.isEmpty()) {
            new Alert(Alert.AlertType.WARNING, "Inserisci un prezzo.").showAndWait();
            return;
        }

        String matricola = Session.getMatricola();
        if (matricola == null || matricola.isBlank()) {
            new Alert(Alert.AlertType.WARNING, "Devi essere loggato per inviare un'offerta.").showAndWait();
            return;
        }

        BigDecimal importo;
        try {
            importo = new BigDecimal(raw.replace(',', '.'));
        } catch (NumberFormatException ex) {
            new Alert(Alert.AlertType.WARNING, "Formato prezzo non valido.").showAndWait();
            return;
        }

        try (Connection conn = DB.getConnection()) {
            if (isSelfOffer(conn, matricola)) {
                new Alert(Alert.AlertType.WARNING, "Non puoi fare un'offerta al tuo annuncio.").showAndWait();
                return;
            }
            if (hasActiveOffer(conn, matricola)) {
                new Alert(Alert.AlertType.WARNING, "Hai gia inviato un'offerta per questo annuncio.").showAndWait();
                return;
            }
            insertOfferta(conn, matricola, importo, txtNota.getText());
            new Alert(Alert.AlertType.INFORMATION, "Offerta inviata!").showAndWait();
        } catch (Exception ex) {
            new Alert(Alert.AlertType.ERROR, "Errore durante l'invio dell'offerta.").showAndWait();
            ex.printStackTrace();
        }
    }

    private int insertOfferta(Connection conn, String matricola, BigDecimal importo, String messaggio) throws Exception {
        String sql = """
            INSERT INTO offerta (id_annuncio, matricola_offerente, stato, importo_proposto, messaggio, data_offerta)
            VALUES (?, ?, ?::stato_offerta_enum, ?, ?, now())
            RETURNING id_offerta
        """;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, ann.getIdAnnuncio());
            ps.setString(2, matricola);
            ps.setString(3, "in_attesa");
            ps.setBigDecimal(4, importo);

            if (messaggio == null || messaggio.isBlank()) ps.setNull(5, Types.VARCHAR);
            else ps.setString(5, messaggio.trim());

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        }
        throw new Exception("Impossibile ottenere id_offerta (RETURNING vuoto)");
    }

    private boolean hasActiveOffer(Connection conn, String matricola) throws Exception {
        String sql = """
            SELECT 1
            FROM offerta
            WHERE id_annuncio = ?
              AND matricola_offerente = ?
              AND stato NOT IN ('rifiutata'::stato_offerta_enum, 'ritirata'::stato_offerta_enum)
            LIMIT 1
        """;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, ann.getIdAnnuncio());
            ps.setString(2, matricola);
            return ps.executeQuery().next();
        }
    }

    private boolean isSelfOffer(Connection conn, String matricola) throws Exception {
        String sql = "SELECT matricola_venditore FROM annuncio WHERE id_annuncio = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, ann.getIdAnnuncio());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String venditore = rs.getString(1);
                    return matricola.equalsIgnoreCase(venditore);
                }
            }
        }
        return false;
    }
}
