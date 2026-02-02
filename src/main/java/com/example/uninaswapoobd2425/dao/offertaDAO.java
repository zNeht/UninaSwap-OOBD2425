package com.example.uninaswapoobd2425.dao;

import com.example.uninaswapoobd2425.model.annuncio;
import com.example.uninaswapoobd2425.model.offerta;
import com.example.uninaswapoobd2425.model.statoAnnuncio;
import com.example.uninaswapoobd2425.model.statoOfferta;
import com.example.uninaswapoobd2425.model.tipoAnnuncio;
import com.example.uninaswapoobd2425.model.utente;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class offertaDAO {
    private final Connection conn;

    // Crea il DAO con una connessione gia' aperta.
    public offertaDAO(Connection conn) {
        this.conn = conn;
    }

    // Carica le offerte ricevute dal venditore (con dati annuncio e stato).
    public List<offerta> getOfferteRicevute(String matricolaVenditore) throws Exception {
        String sql = """
            SELECT o.id_offerta,
                   o.id_annuncio,
                   o.stato,
                   o.importo_proposto,
                   o.messaggio,
                   o.matricola_offerente,
                   o.data_offerta,
                   a.titolo,
                   a.matricola_venditore,
                   a.tipo,
                   a.stato AS stato_annuncio,
                   a.prezzo AS prezzo_richiesto,
                   ia.path AS img_path
            FROM offerta o
            JOIN annuncio a ON a.id_annuncio = o.id_annuncio
            LEFT JOIN immagine_annuncio ia
                   ON ia.id_annuncio = a.id_annuncio
                  AND ia.is_principale = true
            WHERE a.matricola_venditore = ?
            ORDER BY o.data_offerta DESC NULLS LAST, o.id_offerta DESC
        """;

        List<offerta> out = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, matricolaVenditore);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    // Mappa i campi dell'offerta.
                    offerta v = new offerta();
                    v.setIdOfferta(rs.getInt("id_offerta"));
                    v.setStato(statoOfferta.valueOf(rs.getString("stato")));
                    v.setImportoProposto(rs.getBigDecimal("importo_proposto"));
                    v.setMessaggio(rs.getString("messaggio"));
                    // Mappa offerente minimo (matricola).
                    utente offerente = new utente();
                    offerente.setMatricola(rs.getString("matricola_offerente"));
                    v.setOfferente(offerente);
                    var ts = rs.getTimestamp("data_offerta");
                    v.setDataOfferta(ts != null ? ts.toLocalDateTime() : null);

                    // Mappa l'annuncio collegato.
                    annuncio a = new annuncio();
                    a.setIdAnnuncio(rs.getInt("id_annuncio"));
                    a.setTitolo(rs.getString("titolo"));
                    utente venditore = new utente();
                    venditore.setMatricola(rs.getString("matricola_venditore"));
                    a.setVenditore(venditore);
                    a.setTipo(tipoAnnuncio.valueOf(rs.getString("tipo")));
                    String sa = rs.getString("stato_annuncio");
                    a.setStato((sa != null && !sa.isBlank()) ? statoAnnuncio.valueOf(sa) : statoAnnuncio.attivo);
                    a.setPrezzo(rs.getBigDecimal("prezzo_richiesto"));
                    a.setImmaginePath(rs.getString("img_path"));
                    v.setAnnuncio(a);

                    out.add(v);
                }
            }
        }
        return out;
    }

    // Carica le offerte inviate dall'utente.
    public List<offerta> getOfferteInviate(String matricolaOfferente) throws Exception {
        String sql = """
            SELECT o.id_offerta,
                   o.id_annuncio,
                   o.stato,
                   o.importo_proposto,
                   o.messaggio,
                   o.matricola_offerente,
                   o.data_offerta,
                   a.titolo,
                   a.matricola_venditore,
                   a.tipo,
                   a.stato AS stato_annuncio,
                   a.prezzo AS prezzo_richiesto,
                   ia.path AS img_path
            FROM offerta o
            JOIN annuncio a ON a.id_annuncio = o.id_annuncio
            LEFT JOIN immagine_annuncio ia
                   ON ia.id_annuncio = a.id_annuncio
                  AND ia.is_principale = true
            WHERE o.matricola_offerente = ?
            ORDER BY o.data_offerta DESC NULLS LAST, o.id_offerta DESC
        """;

        List<offerta> out = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, matricolaOfferente);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    // Mappa i campi dell'offerta.
                    offerta v = new offerta();
                    v.setIdOfferta(rs.getInt("id_offerta"));
                    v.setStato(statoOfferta.valueOf(rs.getString("stato")));
                    v.setImportoProposto(rs.getBigDecimal("importo_proposto"));
                    v.setMessaggio(rs.getString("messaggio"));
                    // Mappa offerente minimo (matricola).
                    utente offerente = new utente();
                    offerente.setMatricola(rs.getString("matricola_offerente"));
                    v.setOfferente(offerente);
                    var ts = rs.getTimestamp("data_offerta");
                    v.setDataOfferta(ts != null ? ts.toLocalDateTime() : null);

                    // Mappa l'annuncio collegato.
                    annuncio a = new annuncio();
                    a.setIdAnnuncio(rs.getInt("id_annuncio"));
                    a.setTitolo(rs.getString("titolo"));
                    utente venditore = new utente();
                    venditore.setMatricola(rs.getString("matricola_venditore"));
                    a.setVenditore(venditore);
                    a.setTipo(tipoAnnuncio.valueOf(rs.getString("tipo")));
                    String sa2 = rs.getString("stato_annuncio");
                    a.setStato((sa2 != null && !sa2.isBlank()) ? statoAnnuncio.valueOf(sa2) : statoAnnuncio.attivo);
                    a.setPrezzo(rs.getBigDecimal("prezzo_richiesto"));
                    a.setImmaginePath(rs.getString("img_path"));
                    v.setAnnuncio(a);

                    out.add(v);
                }
            }
        }
        return out;
    }

    // Aggiorna lo stato di una singola offerta.
    public void aggiornaStato(int idOfferta, statoOfferta stato) throws Exception {
        String sql = "UPDATE offerta SET stato = ?::stato_offerta_enum WHERE id_offerta = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, stato.name());
            ps.setInt(2, idOfferta);
            ps.executeUpdate();
        }
    }

    // Rifiuta automaticamente tutte le altre offerte in attesa per lo stesso annuncio.
    public void rifiutaAltreOfferteInAttesa(int idAnnuncio, int idOffertaAccettata) throws Exception {
        String sql = """
            UPDATE offerta
               SET stato = 'rifiutata'::stato_offerta_enum
             WHERE id_annuncio = ?
               AND id_offerta <> ?
               AND stato = 'in_attesa'::stato_offerta_enum
        """;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idAnnuncio);
            ps.setInt(2, idOffertaAccettata);
            ps.executeUpdate();
        }
    }

    // Crea una nuova offerta di scambio e ritorna l'id generato.
    public int creaOffertaScambio(int idAnnuncio, String matricola, String messaggio) throws Exception {
        String sql = """
            INSERT INTO offerta (id_annuncio, matricola_offerente, stato, importo_proposto, messaggio, data_offerta)
            VALUES (?, ?, ?::stato_offerta_enum, NULL, ?, now())
            RETURNING id_offerta
        """;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idAnnuncio);
            ps.setString(2, matricola);
            ps.setString(3, statoOfferta.in_attesa.name());
            if (messaggio == null || messaggio.isBlank()) ps.setNull(4, java.sql.Types.VARCHAR);
            else ps.setString(4, messaggio.trim());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        }
        throw new Exception("Impossibile ottenere id_offerta");
    }

    // Aggiorna solo il messaggio di una offerta.
    public void aggiornaMessaggio(int idOfferta, String messaggio) throws Exception {
        String sql = "UPDATE offerta SET messaggio = ?, data_offerta = now() WHERE id_offerta = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            if (messaggio == null || messaggio.isBlank()) ps.setNull(1, java.sql.Types.VARCHAR);
            else ps.setString(1, messaggio.trim());
            ps.setInt(2, idOfferta);
            ps.executeUpdate();
        }
    }

    // Aggiorna solo l'importo proposto.
    public void aggiornaImporto(int idOfferta, BigDecimal importo) throws Exception {
        String sql = "UPDATE offerta SET importo_proposto = ?, data_offerta = now() WHERE id_offerta = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            if (importo == null) ps.setNull(1, java.sql.Types.NUMERIC);
            else ps.setBigDecimal(1, importo);
            ps.setInt(2, idOfferta);
            ps.executeUpdate();
        }
    }

    // Aggiorna importo e messaggio in un'unica query.
    public void aggiornaImportoEMessaggio(int idOfferta, BigDecimal importo, String messaggio) throws Exception {
        String sql = "UPDATE offerta SET importo_proposto = ?, messaggio = ?, data_offerta = now() WHERE id_offerta = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            if (importo == null) ps.setNull(1, java.sql.Types.NUMERIC);
            else ps.setBigDecimal(1, importo);
            if (messaggio == null || messaggio.isBlank()) ps.setNull(2, java.sql.Types.VARCHAR);
            else ps.setString(2, messaggio.trim());
            ps.setInt(3, idOfferta);
            ps.executeUpdate();
        }
    }

    // Aggiorna importo, messaggio e stato (usato per reinvio/modifica).
    public void aggiornaImportoEMessaggioEStato(int idOfferta, BigDecimal importo, String messaggio, statoOfferta stato) throws Exception {
        String sql = "UPDATE offerta SET importo_proposto = ?, messaggio = ?, stato = ?::stato_offerta_enum, data_offerta = now() WHERE id_offerta = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            if (importo == null) ps.setNull(1, java.sql.Types.NUMERIC);
            else ps.setBigDecimal(1, importo);
            if (messaggio == null || messaggio.isBlank()) ps.setNull(2, java.sql.Types.VARCHAR);
            else ps.setString(2, messaggio.trim());
            ps.setString(3, stato.name());
            ps.setInt(4, idOfferta);
            ps.executeUpdate();
        }
    }
}
