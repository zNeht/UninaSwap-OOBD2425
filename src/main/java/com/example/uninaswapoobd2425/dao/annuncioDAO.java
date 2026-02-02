package com.example.uninaswapoobd2425.dao;

import com.example.uninaswapoobd2425.model.annuncio;
import com.example.uninaswapoobd2425.model.categoriaAnnuncio;
import com.example.uninaswapoobd2425.model.tipoAnnuncio;
import com.example.uninaswapoobd2425.model.statoAnnuncio;
import com.example.uninaswapoobd2425.model.utente;

import java.util.ArrayList;
import java.util.List;
import java.math.BigDecimal;
import java.sql.*;

public class annuncioDAO {

    private final Connection conn;

    // Crea il DAO con una connessione gia' aperta.
    public annuncioDAO(Connection conn) {
        this.conn = conn;
    }

    // Carica tutti gli annunci attivi con immagine principale (se presente).
    public List<annuncio> getAnnunciAttiviConImgPrincipale() throws SQLException {

        String sql = """
        SELECT a.id_annuncio,
               a.titolo,
               a.descrizione,
               a.prezzo,
               a.categoria,
               a.tipo,
               a.stato,
               u.matricola AS venditore_matricola,
               u.mail AS venditore_mail,
               ia.path AS img_path
        FROM annuncio a
        JOIN utente u
          ON u.matricola = a.matricola_venditore
        LEFT JOIN immagine_annuncio ia
               ON ia.id_annuncio = a.id_annuncio
              AND ia.is_principale = true
        WHERE a.stato = 'attivo'::stato_annuncio_enum
        ORDER BY a.data DESC, a.id_annuncio DESC
    """;

        List<annuncio> out = new ArrayList<>();

        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                annuncio an = new annuncio();

                an.setIdAnnuncio(rs.getInt("id_annuncio"));
                an.setTitolo(rs.getString("titolo"));
                an.setDescrizione(rs.getString("descrizione"));
                an.setPrezzo(rs.getBigDecimal("prezzo"));

                an.setCategoria(parseCategoria(rs.getString("categoria")));
                an.setTipo(parseTipo(rs.getString("tipo")));
                an.setStato(parseStato(rs.getString("stato")));

                // Mappa il venditore minimo necessario per UI (matricola + mail).
                utente venditore = new utente();
                venditore.setMatricola(rs.getString("venditore_matricola"));
                venditore.setMail(rs.getString("venditore_mail"));
                an.setVenditore(venditore);
                an.setImmaginePath(rs.getString("img_path"));

                out.add(an);
            }
        }

        return out;
    }

    // Carica annunci attivi filtrati per tipo.
    public List<annuncio> getAnnunciAttiviConImgPrincipaleByTipo(tipoAnnuncio tipo) throws SQLException {
        String sql = """
        SELECT a.id_annuncio,
               a.titolo,
               a.descrizione,
               a.prezzo,
               a.categoria,
               a.tipo,
               a.stato,
               u.matricola AS venditore_matricola,
               u.mail AS venditore_mail,
               ia.path AS img_path
        FROM annuncio a
        JOIN utente u
          ON u.matricola = a.matricola_venditore
        LEFT JOIN immagine_annuncio ia
               ON ia.id_annuncio = a.id_annuncio
              AND ia.is_principale = true
        WHERE a.stato = 'attivo'::stato_annuncio_enum
          AND a.tipo = ?::tipo_annuncio_enum
        ORDER BY a.data DESC, a.id_annuncio DESC
    """;

        List<annuncio> out = new ArrayList<>();

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, tipo.name());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    annuncio an = new annuncio();

                    an.setIdAnnuncio(rs.getInt("id_annuncio"));
                    an.setTitolo(rs.getString("titolo"));
                    an.setDescrizione(rs.getString("descrizione"));
                    an.setPrezzo(rs.getBigDecimal("prezzo"));

                    an.setCategoria(parseCategoria(rs.getString("categoria")));
                    an.setTipo(parseTipo(rs.getString("tipo")));
                    an.setStato(parseStato(rs.getString("stato")));

                    // Mappa il venditore minimo necessario per UI.
                    utente venditore = new utente();
                    venditore.setMatricola(rs.getString("venditore_matricola"));
                    venditore.setMail(rs.getString("venditore_mail"));
                    an.setVenditore(venditore);
                    an.setImmaginePath(rs.getString("img_path"));

                    out.add(an);
                }
            }
        }

        return out;
    }

    public List<annuncio> getAnnunciAttiviConImgPrincipaleByTipoAndCategoria(
            tipoAnnuncio tipo,
            categoriaAnnuncio categoria
    ) throws SQLException {
        String sql = """
        SELECT a.id_annuncio,
               a.titolo,
               a.descrizione,
               a.prezzo,
               a.categoria,
               a.tipo,
               a.stato,
               u.matricola AS venditore_matricola,
               u.mail AS venditore_mail,
               ia.path AS img_path
        FROM annuncio a
        JOIN utente u
          ON u.matricola = a.matricola_venditore
        LEFT JOIN immagine_annuncio ia
               ON ia.id_annuncio = a.id_annuncio
              AND ia.is_principale = true
        WHERE a.stato = 'attivo'::stato_annuncio_enum
          AND a.tipo = ?::tipo_annuncio_enum
          AND a.categoria = ?::categoria_annuncio_enum
        ORDER BY a.data DESC, a.id_annuncio DESC
    """;

        List<annuncio> out = new ArrayList<>();

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, tipo.name());
            ps.setString(2, categoria.name());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    annuncio an = new annuncio();

                    an.setIdAnnuncio(rs.getInt("id_annuncio"));
                    an.setTitolo(rs.getString("titolo"));
                    an.setDescrizione(rs.getString("descrizione"));
                    an.setPrezzo(rs.getBigDecimal("prezzo"));

                    an.setCategoria(parseCategoria(rs.getString("categoria")));
                    an.setTipo(parseTipo(rs.getString("tipo")));
                    an.setStato(parseStato(rs.getString("stato")));

                    // Mappa il venditore minimo necessario per UI.
                    utente venditore = new utente();
                    venditore.setMatricola(rs.getString("venditore_matricola"));
                    venditore.setMail(rs.getString("venditore_mail"));
                    an.setVenditore(venditore);
                    an.setImmaginePath(rs.getString("img_path"));

                    out.add(an);
                }
            }
        }

        return out;
    }

    // Carica gli annunci preferiti di un utente (solo attivi).
    public List<annuncio> getAnnunciPreferitiByUtente(String matricola) throws SQLException {
        String sql = """
        SELECT a.id_annuncio,
               a.titolo,
               a.descrizione,
               a.prezzo,
               a.categoria,
               a.tipo,
               a.stato,
               u.matricola AS venditore_matricola,
               u.mail AS venditore_mail,
               ia.path AS img_path
        FROM wishlist w
        JOIN annuncio a
          ON a.id_annuncio = w.id_annuncio
        JOIN utente u
          ON u.matricola = a.matricola_venditore
        LEFT JOIN immagine_annuncio ia
               ON ia.id_annuncio = a.id_annuncio
              AND ia.is_principale = true
        WHERE w.id_utente = ?
          AND a.stato = 'attivo'::stato_annuncio_enum
        ORDER BY a.data DESC, a.id_annuncio DESC
    """;

        List<annuncio> out = new ArrayList<>();

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, matricola);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    annuncio an = new annuncio();

                    an.setIdAnnuncio(rs.getInt("id_annuncio"));
                    an.setTitolo(rs.getString("titolo"));
                    an.setDescrizione(rs.getString("descrizione"));
                    an.setPrezzo(rs.getBigDecimal("prezzo"));

                    an.setCategoria(parseCategoria(rs.getString("categoria")));
                    an.setTipo(parseTipo(rs.getString("tipo")));
                    an.setStato(parseStato(rs.getString("stato")));

                    // Mappa il venditore minimo necessario per UI.
                    utente venditore = new utente();
                    venditore.setMatricola(rs.getString("venditore_matricola"));
                    venditore.setMail(rs.getString("venditore_mail"));
                    an.setVenditore(venditore);
                    an.setImmaginePath(rs.getString("img_path"));

                    out.add(an);
                }
            }
        }

        return out;
    }

    public List<annuncio> getAnnunciPreferitiByUtenteAndCategoria(String matricola, categoriaAnnuncio categoria)
            throws SQLException {
        String sql = """
        SELECT a.id_annuncio,
               a.titolo,
               a.descrizione,
               a.prezzo,
               a.categoria,
               a.tipo,
               a.stato,
               u.matricola AS venditore_matricola,
               u.mail AS venditore_mail,
               ia.path AS img_path
        FROM wishlist w
        JOIN annuncio a
          ON a.id_annuncio = w.id_annuncio
        JOIN utente u
          ON u.matricola = a.matricola_venditore
        LEFT JOIN immagine_annuncio ia
               ON ia.id_annuncio = a.id_annuncio
              AND ia.is_principale = true
        WHERE w.id_utente = ?
          AND a.stato = 'attivo'::stato_annuncio_enum
          AND a.categoria = ?::categoria_annuncio_enum
        ORDER BY a.data DESC, a.id_annuncio DESC
    """;

        List<annuncio> out = new ArrayList<>();

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, matricola);
            ps.setString(2, categoria.name());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    annuncio an = new annuncio();

                    an.setIdAnnuncio(rs.getInt("id_annuncio"));
                    an.setTitolo(rs.getString("titolo"));
                    an.setDescrizione(rs.getString("descrizione"));
                    an.setPrezzo(rs.getBigDecimal("prezzo"));

                    an.setCategoria(parseCategoria(rs.getString("categoria")));
                    an.setTipo(parseTipo(rs.getString("tipo")));
                    an.setStato(parseStato(rs.getString("stato")));

                    // Mappa il venditore minimo necessario per UI.
                    utente venditore = new utente();
                    venditore.setMatricola(rs.getString("venditore_matricola"));
                    venditore.setMail(rs.getString("venditore_mail"));
                    an.setVenditore(venditore);
                    an.setImmaginePath(rs.getString("img_path"));

                    out.add(an);
                }
            }
        }

        return out;
    }

    // Carica un annuncio per id con venditore e immagine principale.
    public annuncio getAnnuncioById(int idAnnuncio) throws SQLException {
        String sql = """
        SELECT a.id_annuncio,
               a.titolo,
               a.descrizione,
               a.prezzo,
               a.categoria,
               a.tipo,
               a.stato,
               u.matricola AS venditore_matricola,
               u.mail AS venditore_mail,
               ia.path AS img_path
        FROM annuncio a
        JOIN utente u
          ON u.matricola = a.matricola_venditore
        LEFT JOIN immagine_annuncio ia
               ON ia.id_annuncio = a.id_annuncio
              AND ia.is_principale = true
        WHERE a.id_annuncio = ?
        """;

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idAnnuncio);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    annuncio an = new annuncio();
                    an.setIdAnnuncio(rs.getInt("id_annuncio"));
                    an.setTitolo(rs.getString("titolo"));
                    an.setDescrizione(rs.getString("descrizione"));
                    an.setPrezzo(rs.getBigDecimal("prezzo"));
                    an.setCategoria(parseCategoria(rs.getString("categoria")));
                    an.setTipo(parseTipo(rs.getString("tipo")));
                    an.setStato(parseStato(rs.getString("stato")));
                    // Mappa il venditore minimo necessario per UI.
                    utente venditore = new utente();
                    venditore.setMatricola(rs.getString("venditore_matricola"));
                    venditore.setMail(rs.getString("venditore_mail"));
                    an.setVenditore(venditore);
                    an.setImmaginePath(rs.getString("img_path"));
                    return an;
                }
            }
        }
        return null;
    }

    // Inserisce un nuovo annuncio e restituisce l'id generato.
    public int insertAnnuncioReturningId(
            String titolo,
            String descrizione,
            BigDecimal prezzo,      // <-- BigDecimal
            Date data,
            String matricolaVenditore,
            String categoria,
            String stato,
            String tipo
    ) throws SQLException {

        String sql = """
            INSERT INTO annuncio (titolo, descrizione, prezzo, data, matricola_venditore, categoria, tipo, stato)
            VALUES (?, ?, ?, ?, ?, ?::categoria_annuncio_enum, ?::tipo_annuncio_enum, ?::stato_annuncio_enum)
            RETURNING id_annuncio
        """;

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, titolo);
            ps.setString(2, descrizione);

            if (prezzo == null) ps.setNull(3, Types.NUMERIC);
            else ps.setBigDecimal(3, prezzo);

            ps.setDate(4, data);
            ps.setString(5, matricolaVenditore);
            ps.setString(6, categoria);
            ps.setString(7, tipo);
            ps.setString(8, stato);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt("id_annuncio");
            }
        }

        throw new SQLException("Impossibile ottenere id_annuncio (RETURNING vuoto)");
    }

    private categoriaAnnuncio parseCategoria(String val) {
        if (val == null || val.isBlank()) return null;
        return categoriaAnnuncio.valueOf(val.toLowerCase());
    }

    // Converte la stringa DB in enum tipoAnnuncio.
    private tipoAnnuncio parseTipo(String val) {
        if (val == null || val.isBlank()) return null;
        return tipoAnnuncio.valueOf(val.toLowerCase());
    }

    // Converte la stringa DB in enum statoAnnuncio.
    private statoAnnuncio parseStato(String val) {
        if (val == null || val.isBlank()) return null;
        return statoAnnuncio.valueOf(val.toLowerCase());
    }
}
