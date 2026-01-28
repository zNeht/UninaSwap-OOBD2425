package com.example.uninaswapoobd2425.controller;

import com.example.uninaswapoobd2425.dao.DB;
import com.example.uninaswapoobd2425.dao.statisticheDAO;
import com.example.uninaswapoobd2425.model.Session;
import javafx.fxml.FXML;
import javafx.scene.chart.PieChart;
import javafx.scene.control.Label;

import java.sql.Connection;

public class statisticheController {
    @FXML private PieChart pieInviate;
    @FXML private PieChart pieAccettate;
    @FXML private Label lblStatsVendita;
    @FXML private Label lblInviateVendita;
    @FXML private Label lblInviateScambio;
    @FXML private Label lblInviateRegalo;
    @FXML private Label lblAccettateVendita;
    @FXML private Label lblAccettateScambio;
    @FXML private Label lblAccettateRegalo;
    @FXML private Label lblAccettateVenditore;

    public void loadData() {
        String matricola = Session.getMatricola();
        if (matricola == null || matricola.isBlank()) {
            lblStatsVendita.setText("Devi essere loggato per vedere le statistiche.");
            return;
        }
        try (Connection conn = DB.getConnection()) {
            statisticheDAO dao = new statisticheDAO(conn);
            statisticheDAO.Statistiche s = dao.getStatistiche(matricola);
            renderPie(pieInviate, s.inviateVendita, s.inviateScambio, s.inviateRegalo, "Offerte inviate per tipologia");
            renderPie(pieAccettate, s.accettateVendita, s.accettateScambio, s.accettateRegalo, "Offerte accettate per tipologia");
            lblStatsVendita.setText(formatVenditaStats(s));
            renderCounts(s);
            renderVenditeAccettateDaVenditore(s);
        } catch (Exception ex) {
            ex.printStackTrace();
            lblStatsVendita.setText("Errore nel caricamento delle statistiche");
        }
    }

    private void renderPie(PieChart pie, int vendite, int scambi, int regali, String title) {
        pie.getData().clear();
        pie.setTitle(title);
        if (vendite == 0 && scambi == 0 && regali == 0) {
            pie.setLegendVisible(false);
            pie.setLabelsVisible(false);
            pie.getData().add(new PieChart.Data("Nessun dato", 1));
            return;
        }
        if (vendite > 0) pie.getData().add(new PieChart.Data("Vendita", vendite));
        if (scambi > 0) pie.getData().add(new PieChart.Data("Scambio", scambi));
        if (regali > 0) pie.getData().add(new PieChart.Data("Regalo", regali));
    }

    private String formatVenditaStats(statisticheDAO.Statistiche s) {
        if (s.mediaVendita == null && s.minVendita == null && s.maxVendita == null) {
            return "Nessuna offerta di vendita accettata con importo.";
        }
        String min = s.minVendita != null ? "€ " + s.minVendita : "-";
        String max = s.maxVendita != null ? "€ " + s.maxVendita : "-";
        String avg = s.mediaVendita != null ? "€ " + s.mediaVendita.setScale(2, java.math.RoundingMode.HALF_UP) : "-";
        String count = s.accettateVenditaCount > 0 ? String.valueOf(s.accettateVenditaCount) : "0";
        return "Totale: " + count + "   Min: " + min + "   Max: " + max + "   Media: " + avg;
    }

    private void renderCounts(statisticheDAO.Statistiche s) {
        if (lblInviateVendita != null) {
            lblInviateVendita.setText("Vendita: " + s.inviateVendita);
            lblInviateScambio.setText("Scambio: " + s.inviateScambio);
            lblInviateRegalo.setText("Regalo: " + s.inviateRegalo);
            lblAccettateVendita.setText("Vendita: " + s.accettateVendita);
            lblAccettateScambio.setText("Scambio: " + s.accettateScambio);
            lblAccettateRegalo.setText("Regalo: " + s.accettateRegalo);
        }
    }

    private void renderVenditeAccettateDaVenditore(statisticheDAO.Statistiche s) {
        if (lblAccettateVenditore == null) return;
        if (s.accettateComeVenditoreVendita == 0) {
            lblAccettateVenditore.setText("Nessuna vendita accettata come venditore.");
            return;
        }
        String min = fmtCurrency(s.minAccettateVenditore);
        String max = fmtCurrency(s.maxAccettateVenditore);
        String avg = fmtCurrency(s.mediaAccettateVenditore);
        lblAccettateVenditore.setText("Totale: " + s.accettateComeVenditoreVendita + "   Min: " + min + "   Max: " + max + "   Media: " + avg);
    }

    private String fmtCurrency(java.math.BigDecimal v) {
        if (v == null) return "-";
        return "€ " + v.setScale(2, java.math.RoundingMode.HALF_UP);
    }
}
