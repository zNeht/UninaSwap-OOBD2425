package com.example.uninaswapoobd2425.dao;

import com.example.uninaswapoobd2425.model.tipoAnnuncio;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class statisticheDAO {
    private final Connection conn;

    public statisticheDAO(Connection conn) {
        this.conn = conn;
    }

    public static class Statistiche {
        public int inviateVendita;
        public int inviateScambio;
        public int inviateRegalo;

        public int accettateVendita;
        public int accettateScambio;
        public int accettateRegalo;

        public BigDecimal minVendita;
        public BigDecimal maxVendita;
        public BigDecimal mediaVendita;
    }

    public Statistiche getStatistiche(String matricola) throws Exception {
        Statistiche s = new Statistiche();
        loadInviate(matricola, s);
        loadAccettate(matricola, s);
        loadVenditaAccettateStats(matricola, s);
        return s;
    }

    private void loadInviate(String matricola, Statistiche s) throws Exception {
        String sql = """
            SELECT a.tipo, count(*) AS cnt
            FROM offerta o
            JOIN annuncio a ON a.id_annuncio = o.id_annuncio
            WHERE o.matricola_offerente = ?
            GROUP BY a.tipo
        """;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, matricola);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    tipoAnnuncio t = tipoAnnuncio.valueOf(rs.getString("tipo"));
                    int c = rs.getInt("cnt");
                    switch (t) {
                        case vendita -> s.inviateVendita = c;
                        case scambio -> s.inviateScambio = c;
                        case regalo -> s.inviateRegalo = c;
                    }
                }
            }
        }
    }

    private void loadAccettate(String matricola, Statistiche s) throws Exception {
        String sql = """
            SELECT a.tipo, count(*) AS cnt
            FROM offerta o
            JOIN annuncio a ON a.id_annuncio = o.id_annuncio
            WHERE o.matricola_offerente = ?
              AND o.stato = 'accettata'::stato_offerta_enum
            GROUP BY a.tipo
        """;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, matricola);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    tipoAnnuncio t = tipoAnnuncio.valueOf(rs.getString("tipo"));
                    int c = rs.getInt("cnt");
                    switch (t) {
                        case vendita -> s.accettateVendita = c;
                        case scambio -> s.accettateScambio = c;
                        case regalo -> s.accettateRegalo = c;
                    }
                }
            }
        }
    }

    private void loadVenditaAccettateStats(String matricola, Statistiche s) throws Exception {
        String sql = """
            SELECT min(o.importo_proposto) AS minp,
                   max(o.importo_proposto) AS maxp,
                   avg(o.importo_proposto) AS avgp
            FROM offerta o
            JOIN annuncio a ON a.id_annuncio = o.id_annuncio
            WHERE o.matricola_offerente = ?
              AND o.stato = 'accettata'::stato_offerta_enum
              AND a.tipo = 'vendita'::tipo_annuncio_enum
              AND o.importo_proposto IS NOT NULL
        """;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, matricola);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    s.minVendita = rs.getBigDecimal("minp");
                    s.maxVendita = rs.getBigDecimal("maxp");
                    s.mediaVendita = rs.getBigDecimal("avgp");
                }
            }
        }
    }
}
