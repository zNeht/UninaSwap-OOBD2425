package com.example.uninaswapoobd2425.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;


 //Gestione recensioni su offerte concluse.

public class recensioneDAO {
    private final Connection conn;

    // Crea il DAO con una connessione gia' aperta.
    public recensioneDAO(Connection conn) {
        this.conn = conn;
    }


    // Elimina la recensione esistente (se presente) per consentire il reinvio.
    public void deleteByOffertaAndRecensore(int idOfferta, String matricolaRecensore) throws Exception {
        ensureTable();
        Integer idTrans = findTransazioneId(idOfferta);
        if (idTrans == null) {
            throw new Exception("Nessuna transazione trovata per l'offerta " + idOfferta);
        }
        String sql = "DELETE FROM recensioni WHERE id_transazione = ? AND id_utente_recensore = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idTrans);
            ps.setString(2, matricolaRecensore);
            ps.executeUpdate();
        }
    }


    // Inserisce la recensione (creando prima la tabella se manca).
    public int inserisci(int idOfferta, String matricolaRecensore, String matricolaRecensito, int voto, String commento) throws Exception {
        ensureTable();
        Integer idTrans = findTransazioneId(idOfferta);
        if (idTrans == null) {
            throw new Exception("Nessuna transazione trovata per l'offerta " + idOfferta);
        }
        String sql = """
            INSERT INTO recensioni (id_transazione, id_utente_recensore, id_utente_recensito, voto, commento, data_recensione)
            VALUES (?, ?, ?, ?, ?, now())
            RETURNING id_recensione
        """;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idTrans);
            ps.setString(2, matricolaRecensore);
            ps.setString(3, matricolaRecensito);
            ps.setInt(4, voto);
            if (commento == null || commento.isBlank()) {
                ps.setNull(5, java.sql.Types.VARCHAR);
            } else {
                ps.setString(5, commento.trim());
            }
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }
        throw new Exception("Impossibile inserire recensione");
    }


    // Garantisce l'esistenza della tabella recensioni.
    private void ensureTable() throws Exception {
        String sql = """
            CREATE TABLE IF NOT EXISTS recensioni (
                id_recensione SERIAL PRIMARY KEY,
                id_transazione INT NOT NULL REFERENCES transazione(id_transazione) ON DELETE CASCADE,
                id_utente_recensore VARCHAR(20) NOT NULL REFERENCES utente(matricola) ON DELETE CASCADE,
                id_utente_recensito VARCHAR(20) NOT NULL REFERENCES utente(matricola) ON DELETE CASCADE,
                voto SMALLINT NOT NULL CHECK (voto BETWEEN 1 AND 5),
                commento TEXT,
                data_recensione TIMESTAMP NOT NULL DEFAULT now(),
                UNIQUE (id_transazione, id_utente_recensore)
            );
        """;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.executeUpdate();
        }
    }


    // Verifica presenza di una recensione per offerta+recensore.
    public boolean existsByOffertaAndRecensore(int idOfferta, String matricolaRecensore) throws Exception {
        Integer idTrans = findTransazioneId(idOfferta);
        if (idTrans == null) return false;
        String sql = "SELECT 1 FROM recensioni WHERE id_transazione = ? AND id_utente_recensore = ? LIMIT 1";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idTrans);
            ps.setString(2, matricolaRecensore);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    // Ricava l'id della transazione associata all'offerta.
    private Integer findTransazioneId(int idOfferta) throws Exception {
        String sql = "SELECT id_transazione FROM transazione WHERE id_offerta = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idOfferta);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }
        return null;
    }
}
