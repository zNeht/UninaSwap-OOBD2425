package com.example.uninaswapoobd2425.controller;

import com.example.uninaswapoobd2425.dao.DB;
import com.example.uninaswapoobd2425.model.annuncio;
import com.example.uninaswapoobd2425.model.Session;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Types;

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

        String matricola = Session.getMatricola();
        if (matricola == null || matricola.isBlank()) {
            new Alert(Alert.AlertType.WARNING, "Devi essere loggato per inviare una richiesta.").showAndWait();
            return;
        }

        try (Connection conn = DB.getConnection()) {
            if (isSelfOffer(conn, matricola)) {
                new Alert(Alert.AlertType.WARNING, "Non puoi fare una richiesta al tuo annuncio.").showAndWait();
                return;
            }
            if (hasActiveOffer(conn, matricola)) {
                new Alert(Alert.AlertType.WARNING, "Hai gia inviato una richiesta per questo annuncio.").showAndWait();
                return;
            }
            insertOfferta(conn, matricola, txtMsg.getText());
            new Alert(Alert.AlertType.INFORMATION, "Richiesta inviata!").showAndWait();
        } catch (Exception ex) {
            new Alert(Alert.AlertType.ERROR, "Errore durante l'invio della richiesta.").showAndWait();
            ex.printStackTrace();
        }
    }

    private int insertOfferta(Connection conn, String matricola, String messaggio) throws Exception {
        String sql = """
            INSERT INTO offerta (id_annuncio, matricola_offerente, stato, importo_proposto, messaggio, data_offerta)
            VALUES (?, ?, ?::stato_offerta_enum, ?, ?, now())
            RETURNING id_offerta
        """;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, ann.getIdAnnuncio());
            ps.setString(2, matricola);
            ps.setString(3, "in_attesa");
            ps.setNull(4, Types.NUMERIC);

            String msg = messaggio == null ? "" : messaggio.trim();
            if (msg.isEmpty()) ps.setNull(5, Types.VARCHAR);
            else ps.setString(5, msg);

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
