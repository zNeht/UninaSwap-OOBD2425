package com.example.uninaswapoobd2425.dao;

import com.example.uninaswapoobd2425.controller.ImageHandler;
import com.example.uninaswapoobd2425.model.immagineAnnuncio;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class immagineAnnuncioDAO {

    private final Connection conn;

    // Crea il DAO con una connessione gia' aperta.
    public immagineAnnuncioDAO(Connection conn) {
        this.conn = conn;
    }

    // Inserisce in batch le immagini collegate a un annuncio.
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

    // Restituisce i soli path delle immagini di un annuncio (ordine coerente con UI).
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

    // Restituisce i record completi delle immagini di un annuncio.
    public List<immagineAnnuncio> getImagesByAnnuncio(int idAnnuncio) throws SQLException {
        String sql = """
            SELECT id_immagine, id_annuncio, path, ordine, is_principale, uploaded_at
            FROM immagine_annuncio
            WHERE id_annuncio = ?
            ORDER BY is_principale DESC, ordine ASC, uploaded_at ASC
        """;
        List<immagineAnnuncio> out = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idAnnuncio);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    // Mappa ogni riga in un oggetto immagineAnnuncio.
                    immagineAnnuncio img = new immagineAnnuncio();
                    img.setIdImmagine(rs.getInt("id_immagine"));
                    img.setIdAnnuncio(rs.getInt("id_annuncio"));
                    img.setPath(rs.getString("path"));
                    img.setOrdine(rs.getInt("ordine"));
                    img.setPrincipale(rs.getBoolean("is_principale"));
                    Timestamp ts = rs.getTimestamp("uploaded_at");
                    img.setUploadedAt(ts != null ? ts.toLocalDateTime() : null);
                    out.add(img);
                }
            }
        }
        return out;
    }
}
