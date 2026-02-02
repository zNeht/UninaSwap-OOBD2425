package com.example.uninaswapoobd2425.dao;

import com.example.uninaswapoobd2425.model.wishlist;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class wishlistDAO {
    private final Connection conn;

    // Crea il DAO con una connessione gia' aperta.
    public wishlistDAO(Connection conn) {
        this.conn = conn;
    }

    // Verifica se l'utente ha gia' aggiunto l'annuncio ai preferiti.
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

    // Inserisce un record di wishlist (ignora duplicati).
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

    // Rimuove un record di wishlist.
    public void remove(int idAnnuncio, String idUtente) throws Exception {
        String sql = "DELETE FROM wishlist WHERE id_annuncio = ? AND id_utente = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idAnnuncio);
            ps.setString(2, idUtente);
            ps.executeUpdate();
        }
    }

    // Restituisce il numero di preferiti per un annuncio.
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

    // Restituisce tutti i preferiti dell'utente.
    public List<wishlist> getByUtente(String idUtente) throws Exception {
        String sql = """
            SELECT id_wishlist, id_annuncio, id_utente
            FROM wishlist
            WHERE id_utente = ?
            ORDER BY id_wishlist DESC
        """;
        List<wishlist> out = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, idUtente);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    wishlist w = new wishlist();
                    w.setIdWishlist(rs.getInt("id_wishlist"));
                    w.setIdAnnuncio(rs.getInt("id_annuncio"));
                    w.setIdUtente(rs.getString("id_utente"));
                    out.add(w);
                }
            }
        }
        return out;
    }
}
