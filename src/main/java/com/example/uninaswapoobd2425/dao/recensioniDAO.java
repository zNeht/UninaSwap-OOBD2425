package com.example.uninaswapoobd2425.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Lettura recensioni inviate/ricevute.
 */
public class recensioniDAO {
    private final Connection conn;

    public recensioniDAO(Connection conn) {
        this.conn = conn;
    }

    public static class RecensioneView {
        public int idRecensione;
        public int idTransazione;
        public int idAnnuncio;
        public String titoloAnnuncio;
        public String recensore;
        public String recensito;
        public int voto;
        public String commento;
        public LocalDateTime data;
    }

    public List<RecensioneView> getRicevute(String matricola) throws Exception {
        String sql = """
            SELECT r.id_recensione,
                   r.id_transazione,
                   t.id_annuncio,
                   a.titolo,
                   r.id_utente_recensore,
                   r.id_utente_recensito,
                   r.voto,
                   r.commento,
                   r.data_recensione
            FROM recensioni r
            JOIN transazione t ON t.id_transazione = r.id_transazione
            JOIN annuncio a ON a.id_annuncio = t.id_annuncio
            WHERE r.id_utente_recensito = ?
            ORDER BY r.data_recensione DESC NULLS LAST, r.id_recensione DESC
        """;
        return load(sql, matricola);
    }

    public List<RecensioneView> getInviate(String matricola) throws Exception {
        String sql = """
            SELECT r.id_recensione,
                   r.id_transazione,
                   t.id_annuncio,
                   a.titolo,
                   r.id_utente_recensore,
                   r.id_utente_recensito,
                   r.voto,
                   r.commento,
                   r.data_recensione
            FROM recensioni r
            JOIN transazione t ON t.id_transazione = r.id_transazione
            JOIN annuncio a ON a.id_annuncio = t.id_annuncio
            WHERE r.id_utente_recensore = ?
            ORDER BY r.data_recensione DESC NULLS LAST, r.id_recensione DESC
        """;
        return load(sql, matricola);
    }

    private List<RecensioneView> load(String sql, String matricola) throws Exception {
        List<RecensioneView> out = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, matricola);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    RecensioneView v = new RecensioneView();
                    v.idRecensione = rs.getInt("id_recensione");
                    v.idTransazione = rs.getInt("id_transazione");
                    v.idAnnuncio = rs.getInt("id_annuncio");
                    v.titoloAnnuncio = rs.getString("titolo");
                    v.recensore = rs.getString("id_utente_recensore");
                    v.recensito = rs.getString("id_utente_recensito");
                    v.voto = rs.getInt("voto");
                    v.commento = rs.getString("commento");
                    var ts = rs.getTimestamp("data_recensione");
                    v.data = ts != null ? ts.toLocalDateTime() : null;
                    out.add(v);
                }
            }
        }
        return out;
    }
}
