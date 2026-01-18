package com.example.uninaswapoobd2425.dao;

import java.math.BigDecimal;
import java.sql.*;

public class annuncioDAO {

    private final Connection conn;

    public annuncioDAO(Connection conn) {
        this.conn = conn;
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
            INSERT INTO annuncio (titolo, descrizione, prezzo, data, matricola_venditore, categoria, stato, tipo)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?)
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
            ps.setString(7, stato);
            ps.setString(8, tipo);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt("id_annuncio");
            }
        }

        throw new SQLException("Impossibile ottenere id_annuncio (RETURNING vuoto)");
    }
}
