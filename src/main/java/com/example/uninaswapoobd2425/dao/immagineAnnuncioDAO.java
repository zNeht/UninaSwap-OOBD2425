package com.example.uninaswapoobd2425.dao;

import com.example.uninaswapoobd2425.controller.ImageHandler;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class immagineAnnuncioDAO {

    private final Connection conn;

    public immagineAnnuncioDAO(Connection conn) {
        this.conn = conn;
    }

    public void insertImages(int idAnnuncio, List<ImageHandler.SavedImage> imgs) throws SQLException {
        if (imgs == null || imgs.isEmpty()) return;

        String sql = """
            INSERT INTO immagine_annuncio (id_annuncio, path, ordine, is_principale, uploaded_at)
            VALUES (?, ?, ?, ?, ?)
        """;

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            for (var img : imgs) {
                ps.setInt(1, idAnnuncio);
                ps.setString(2, img.pathRelativoDb());
                ps.setInt(3, img.ordine());
                ps.setBoolean(4, img.isPrincipale());
                ps.setTimestamp(5, Timestamp.valueOf(img.uploadedAt()));
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }

    public List<String> getImagePathsByAnnuncio(int idAnnuncio) throws SQLException {
        String sql = """
            SELECT path
            FROM immagine_annuncio
            WHERE id_annuncio = ?
            ORDER BY is_principale DESC, ordine ASC, uploaded_at ASC
        """;

        List<String> paths = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idAnnuncio);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    paths.add(rs.getString("path"));
                }
            }
        }
        return paths;
    }
}
