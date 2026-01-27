package com.example.uninaswapoobd2425.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class wishlistDAO {
    private final Connection conn;

    public wishlistDAO(Connection conn) {
        this.conn = conn;
    }

    public boolean exists(int idAnnuncio, String idUtente) throws Exception {
        String sql = """
            SELECT 1
            FROM wishlist
            WHERE id_annuncio = ? AND id_utente = ?
            LIMIT 1
        """;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idAnnuncio);
            ps.setString(2, idUtente);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    public void add(int idAnnuncio, String idUtente) throws Exception {
        String sql = """
            INSERT INTO wishlist (id_annuncio, id_utente)
            VALUES (?, ?)
            ON CONFLICT DO NOTHING
        """;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idAnnuncio);
            ps.setString(2, idUtente);
            ps.executeUpdate();
        }
    }

    public void remove(int idAnnuncio, String idUtente) throws Exception {
        String sql = "DELETE FROM wishlist WHERE id_annuncio = ? AND id_utente = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idAnnuncio);
            ps.setString(2, idUtente);
            ps.executeUpdate();
        }
    }

    public int countForAnnuncio(int idAnnuncio) throws Exception {
        String sql = "SELECT COUNT(*) FROM wishlist WHERE id_annuncio = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idAnnuncio);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        }
        return 0;
    }
}
