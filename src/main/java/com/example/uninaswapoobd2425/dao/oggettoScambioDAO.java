package com.example.uninaswapoobd2425.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class oggettoScambioDAO {
    private final Connection conn;

    public oggettoScambioDAO(Connection conn) {
        this.conn = conn;
    }

    public static class ScambioItem {
        public String nome;
        public String path;
    }

    public List<ScambioItem> getByOfferta(int idOfferta) throws Exception {
        String sql = "SELECT nome_oggetto, path FROM oggetto_scambio WHERE id_offerta = ?";
        List<ScambioItem> out = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idOfferta);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    ScambioItem it = new ScambioItem();
                    it.nome = rs.getString("nome_oggetto");
                    it.path = rs.getString("path");
                    out.add(it);
                }
            }
        }
        return out;
    }

    public void deleteByOfferta(int idOfferta) throws Exception {
        String sql = "DELETE FROM oggetto_scambio WHERE id_offerta = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idOfferta);
            ps.executeUpdate();
        }
    }

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
