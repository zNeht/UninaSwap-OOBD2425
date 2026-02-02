package com.example.uninaswapoobd2425.dao;

import com.example.uninaswapoobd2425.model.oggettoScambio;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class oggettoScambioDAO {
    private final Connection conn;

    // Crea il DAO con una connessione gia' aperta.
    public oggettoScambioDAO(Connection conn) {
        this.conn = conn;
    }

    // Carica gli oggetti di scambio legati a un'offerta.
    public List<oggettoScambio> getByOfferta(int idOfferta) throws Exception {
        String sql = "SELECT nome_oggetto, path FROM oggetto_scambio WHERE id_offerta = ?";
        List<oggettoScambio> out = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idOfferta);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    // Mappa la riga in oggettoScambio.
                    oggettoScambio it = new oggettoScambio();
                    it.setNomeOggetto(rs.getString("nome_oggetto"));
                    it.setPath(rs.getString("path"));
                    out.add(it);
                }
            }
        }
        return out;
    }

    // Elimina tutti gli oggetti associati a un'offerta.
    public void deleteByOfferta(int idOfferta) throws Exception {
        String sql = "DELETE FROM oggetto_scambio WHERE id_offerta = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idOfferta);
            ps.executeUpdate();
        }
    }

    // Inserisce un singolo oggetto di scambio.
    public void insert(int idOfferta, String nome, String path) throws Exception {
        String sql = "INSERT INTO oggetto_scambio (id_offerta, nome_oggetto, path) VALUES (?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idOfferta);
            ps.setString(2, nome);
            ps.setString(3, path);
            ps.executeUpdate();
        }
    }
}
