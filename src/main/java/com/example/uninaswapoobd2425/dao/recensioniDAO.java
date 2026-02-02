package com.example.uninaswapoobd2425.dao;

import com.example.uninaswapoobd2425.model.recensione;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;


 //Lettura recensioni inviate/ricevute.

public class recensioniDAO {
    private final Connection conn;

    // Crea il DAO con una connessione gia' aperta.
    public recensioniDAO(Connection conn) {
        this.conn = conn;
    }

    // Restituisce le recensioni ricevute dall'utente.
    public List<recensione> getRicevute(String matricola) throws Exception {
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

    // Restituisce le recensioni inviate dall'utente.
    public List<recensione> getInviate(String matricola) throws Exception {
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

    // Esegue la query fornita e mappa il result set in lista di recensioni.
    private List<recensione> load(String sql, String matricola) throws Exception {
        List<recensione> out = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, matricola);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    // Mappa una singola recensione.
                    recensione v = new recensione();
                    v.setIdRecensione(rs.getInt("id_recensione"));
                    v.setIdTransazione(rs.getInt("id_transazione"));
                    v.setIdAnnuncio(rs.getInt("id_annuncio"));
                    v.setTitoloAnnuncio(rs.getString("titolo"));
                    v.setMatricolaRecensore(rs.getString("id_utente_recensore"));
                    v.setMatricolaRecensito(rs.getString("id_utente_recensito"));
                    v.setVoto(rs.getInt("voto"));
                    v.setCommento(rs.getString("commento"));
                    var ts = rs.getTimestamp("data_recensione");
                    v.setDataRecensione(ts != null ? ts.toLocalDateTime() : null);
                    out.add(v);
                }
            }
        }
        return out;
    }
}
