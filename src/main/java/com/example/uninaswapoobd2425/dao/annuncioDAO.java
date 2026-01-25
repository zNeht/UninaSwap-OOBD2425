package com.example.uninaswapoobd2425.dao;

import com.example.uninaswapoobd2425.model.annuncio;
import com.example.uninaswapoobd2425.model.categoriaAnnuncio;
import com.example.uninaswapoobd2425.model.tipoAnnuncio;
import com.example.uninaswapoobd2425.model.statoAnnuncio;

import java.util.ArrayList;
import java.util.List;
import java.math.BigDecimal;
import java.sql.*;

public class annuncioDAO {

    private final Connection conn;

    public annuncioDAO(Connection conn) {
        this.conn = conn;
    }

    public List<annuncio> getAnnunciAttiviConImgPrincipale() throws SQLException {

        String sql = """
        SELECT a.id_annuncio,
               a.titolo,
               a.descrizione,
               a.prezzo,
               a.categoria,
               a.tipo,
               a.stato,
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

                an.setCategoria(categoriaAnnuncio.valueOf(rs.getString("categoria").toLowerCase()));
                an.setTipo(tipoAnnuncio.valueOf(rs.getString("tipo").toLowerCase()));
                an.setStato(statoAnnuncio.valueOf(rs.getString("stato").toLowerCase()));

                an.setVenditoreEmail(rs.getString("venditore_mail"));
                an.setImmaginePath(rs.getString("img_path"));

                out.add(an);
            }
        }

        return out;
    }

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
}
