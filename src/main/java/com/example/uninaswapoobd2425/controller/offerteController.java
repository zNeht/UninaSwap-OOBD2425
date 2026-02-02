package com.example.uninaswapoobd2425.controller;

import com.example.uninaswapoobd2425.dao.DB;
import com.example.uninaswapoobd2425.dao.annuncioDAO;
import com.example.uninaswapoobd2425.dao.offertaDAO;
import com.example.uninaswapoobd2425.dao.oggettoScambioDAO;
import com.example.uninaswapoobd2425.dao.recensioneDAO;
import com.example.uninaswapoobd2425.model.annuncio;
import com.example.uninaswapoobd2425.model.offerta;
import com.example.uninaswapoobd2425.model.oggettoScambio;
import com.example.uninaswapoobd2425.model.Session;
import com.example.uninaswapoobd2425.model.statoAnnuncio;
import com.example.uninaswapoobd2425.model.statoOfferta;
import com.example.uninaswapoobd2425.model.tipoAnnuncio;
import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.Parent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.math.BigDecimal;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.io.File;

public class offerteController {
    @FXML private TilePane tileRicevuteAttesa;
    @FXML private TilePane tileRicevuteAccettate;
    @FXML private TilePane tileRicevuteChiuse;
    @FXML private TilePane tileAccettate;
    @FXML private TilePane tileInviateAttive;
    @FXML private TilePane tileInviateRifiutate;
    @FXML private Button btnToggleRicevuteAttesa;
    @FXML private Button btnToggleRicevuteAccettate;
    @FXML private Button btnToggleRicevuteChiuse;
    @FXML private Button btnToggleAccettate;
    @FXML private Button btnToggleInviateAttive;
    @FXML private Button btnToggleInviateRifiutate;

    // Carica offerte ricevute/inviate per l'utente loggato.
    public void loadData() {
        String matricola = Session.getMatricola();
        if (matricola == null || matricola.isBlank()) {
            return;
        }

        try (Connection conn = DB.getConnection()) {
            offertaDAO dao = new offertaDAO(conn);
            List<offerta> ricevute = dao.getOfferteRicevute(matricola);
            List<offerta> inviate = dao.getOfferteInviate(matricola);

            renderRicevute(ricevute);
            renderInviate(inviate);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    // Renderizza le offerte ricevute nelle tre colonne (attesa/accettate/chiuse).
    private void renderRicevute(List<offerta> list) throws Exception {
        tileRicevuteAttesa.getChildren().clear();
        tileRicevuteAccettate.getChildren().clear();
        tileRicevuteChiuse.getChildren().clear();

        for (offerta v : list) {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(
                    "/com/example/uninaswapoobd2425/offertaCard.fxml"
            ));
            Node card = loader.load();
            offertaCardController c = loader.getController();
            c.setData(v);
            c.setActionsVisible(true);
            c.setOnOpenDetails(() -> openDettaglio(v));
            card.setCursor(javafx.scene.Cursor.HAND);
            card.setPickOnBounds(true);

            annuncio a = v.getAnnuncio();
            boolean inAttesa = v.getStato() == statoOfferta.in_attesa;
            boolean annuncioBloccato = a != null && (a.getStato() == statoAnnuncio.concluso
                    || a.getStato() == statoAnnuncio.annullato);

            // per ricevute: uso i bottoni come Accetta (modifica) / Rifiuta (annulla)
            c.setModificaLabel("Accetta");
            c.setRitiraLabel("Rifiuta offerta");
            c.setHandlers(
                    () -> handleAccetta(v),
                    () -> handleRifiuta(v)
            );
            c.setActionStates(inAttesa && !annuncioBloccato, inAttesa && !annuncioBloccato);
            if (a != null && a.getTipo() == tipoAnnuncio.scambio) {
                c.showItemsButton(true);
                c.setOnViewItems(() -> openOggettiOfferti(v.getIdOfferta()));
            } else {
                c.showItemsButton(false);
            }
            configureMessageButton(c, v);

            if (v.getStato() == statoOfferta.accettata) {
                tileRicevuteAccettate.getChildren().add(card);
            } else if (inAttesa) {
                tileRicevuteAttesa.getChildren().add(card);
            } else {
                tileRicevuteChiuse.getChildren().add(card);
            }
        }
    }

    // Renderizza le offerte inviate nelle tre colonne (accettate/attive/rifiutate).
    private void renderInviate(List<offerta> list) throws Exception {
        tileAccettate.getChildren().clear();
        tileInviateAttive.getChildren().clear();
        tileInviateRifiutate.getChildren().clear();

        String matricola = Session.getMatricola();

        for (offerta v : list) {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(
                    "/com/example/uninaswapoobd2425/offertaCard.fxml"
            ));
            Node card = loader.load();
            offertaCardController c = loader.getController();
            c.setData(v);
            c.showReviewButton(false);
            c.showModifyWithdraw(true);
            c.setOnOpenDetails(() -> openDettaglio(v));
            card.setCursor(javafx.scene.Cursor.HAND);
            card.setPickOnBounds(true);

            annuncio a = v.getAnnuncio();
            boolean inAttesa = v.getStato() == statoOfferta.in_attesa;
            boolean rifiutata = v.getStato() == statoOfferta.rifiutata;
            boolean ritirata = v.getStato() == statoOfferta.ritirata;
            boolean annuncioBloccato = a != null && (a.getStato() == statoAnnuncio.concluso
                    || a.getStato() == statoAnnuncio.annullato);

            boolean canModify = (inAttesa || rifiutata || ritirata) && !annuncioBloccato;
            boolean canWithdraw = inAttesa && !annuncioBloccato;

            String labelModifica = (rifiutata || ritirata) ? "Rinvia offerta" : "Modifica offerta";

            c.setActionStates(canModify, canWithdraw);
            c.setModificaLabel(labelModifica);
            c.setHandlers(
                    () -> {
                        handleModifica(v, rifiutata || ritirata);
                    },
                    () -> handleRitira(v)
            );
            configureMessageButton(c, v);
            configureEditMessageButton(c, v);

            if (v.getStato() == statoOfferta.accettata) {
                boolean alreadyReviewed = matricola != null && !matricola.isBlank()
                        && hasAlreadyReviewed(v.getIdOfferta(), matricola);
                if (alreadyReviewed) {
                    c.showReviewButton(false);
                    c.showModifyWithdraw(false);
                    c.setHandlers(null, null);
                    c.setActionStates(false, false);
                } else {
                    c.showReviewButton(true);
                    c.showModifyWithdraw(false);
                    c.setHandlers(null, null);
                    c.setOnRecensisci(input -> handleRecensione(v, input));
                    c.setActionStates(false, false);
                }
                tileAccettate.getChildren().add(card);
            } else if (inAttesa) {
                tileInviateAttive.getChildren().add(card);
            } else if (rifiutata || ritirata) {
                tileInviateRifiutate.getChildren().add(card);
            } else {
                tileInviateAttive.getChildren().add(card);
            }
        }
    }

    // Configura il bottone messaggio in base al tipo e contenuto.
    private void configureMessageButton(offertaCardController c, offerta v) {
        boolean hasMessage = v.getMessaggio() != null && !v.getMessaggio().isBlank();
        annuncio a = v.getAnnuncio();
        boolean show = a != null && (a.getTipo() == tipoAnnuncio.regalo || hasMessage);
        c.showMessageButton(show);
        if (show) {
            c.setOnViewMessage(() -> openMessaggioOfferta(v.getMessaggio()));
        }
    }

    // Configura il bottone per aggiungere/modificare il messaggio.
    private void configureEditMessageButton(offertaCardController c, offerta v) {
        boolean hasMessage = v.getMessaggio() != null && !v.getMessaggio().isBlank();
        boolean isAccettata = v.getStato() == statoOfferta.accettata;
        boolean isRitirata = v.getStato() == statoOfferta.ritirata;
        boolean canEdit = !isAccettata && !isRitirata;
        c.showEditMessageButton(canEdit);
        if (canEdit) {
            c.setEditMessageLabel(hasMessage ? "Modifica messaggio" : "Aggiungi messaggio");
            c.setOnEditMessage(() -> handleEditMessaggio(v));
        }
    }

    // Verifica se l'utente ha gia' recensito l'offerta.
    private boolean hasAlreadyReviewed(int idOfferta, String matricolaRecensore) {
        try (Connection conn = DB.getConnection()) {
            recensioneDAO dao = new recensioneDAO(conn);
            return dao.existsByOffertaAndRecensore(idOfferta, matricolaRecensore);
        } catch (Exception ex) {
            return false;
        }
    }

    // Ritira un'offerta inviata dall'utente.
    private void handleRitira(offerta v) {
        try (Connection conn = DB.getConnection()) {
            offertaDAO dao = new offertaDAO(conn);
            dao.aggiornaStato(v.getIdOfferta(), statoOfferta.ritirata);
            loadData();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    // Rifiuta un'offerta ricevuta dal venditore.
    private void handleRifiuta(offerta v) {
        try (Connection conn = DB.getConnection()) {
            offertaDAO dao = new offertaDAO(conn);
            dao.aggiornaStato(v.getIdOfferta(), statoOfferta.rifiutata);
            loadData();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    // Accetta un'offerta e chiude l'annuncio in transazione.
    private void handleAccetta(offerta v) {
        try (Connection conn = DB.getConnection()) {
            conn.setAutoCommit(false);
            try {
                offertaDAO dao = new offertaDAO(conn);
                dao.aggiornaStato(v.getIdOfferta(), statoOfferta.accettata);
                dao.rifiutaAltreOfferteInAttesa(v.getAnnuncio().getIdAnnuncio(), v.getIdOfferta());
                try (var ps = conn.prepareStatement("UPDATE annuncio SET stato = 'concluso'::stato_annuncio_enum WHERE id_annuncio = ?")) {
                    ps.setInt(1, v.getAnnuncio().getIdAnnuncio());
                    ps.executeUpdate();
                }
                // Crea transazione se non esiste già per l'offerta accettata
                String insertTrans = """
                    INSERT INTO transazione (id_offerta, id_annuncio, data_conclusione)
                    VALUES (?, ?, now())
                    ON CONFLICT (id_offerta)
                    DO UPDATE SET id_annuncio = EXCLUDED.id_annuncio,
                                  data_conclusione = EXCLUDED.data_conclusione
                """;
                try (var ps = conn.prepareStatement(insertTrans)) {
                    ps.setInt(1, v.getIdOfferta());
                    ps.setInt(2, v.getAnnuncio().getIdAnnuncio());
                    ps.executeUpdate();
                }
                conn.commit();
            } catch (Exception ex) {
                conn.rollback();
                throw ex;
            } finally {
                conn.setAutoCommit(true);
            }
            loadData();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    // Apre il dettaglio annuncio in modale per una offerta.
    private void openDettaglio(offerta v) {
        try (Connection conn = DB.getConnection()) {
            annuncioDAO aDao = new annuncioDAO(conn);
            annuncio a = aDao.getAnnuncioById(v.getAnnuncio().getIdAnnuncio());
            if (a == null) {
                new Alert(Alert.AlertType.WARNING, "Annuncio non trovato.").showAndWait();
                return;
            }

            FXMLLoader loader = new FXMLLoader(getClass().getResource(
                    "/com/example/uninaswapoobd2425/dettaglioAnnuncio.fxml"
            ));
            Parent view = loader.load();
            if (view instanceof Region r) {
                r.setMaxSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);
            }
            dettaglioAnnuncioController c = loader.getController();
            c.setAnnuncioWithOffer(a, v);

            StackPane overlay = findOverlay();
            if (overlay == null) {
                Stage stage = new Stage();
                stage.setScene(new javafx.scene.Scene(view));
                stage.show();
                return;
            }

            overlay.getChildren().setAll(view);
            StackPane.setAlignment(view, Pos.CENTER);
            overlay.setVisible(true);
            overlay.setManaged(true);
            overlay.setMouseTransparent(false);

            c.setOnClose(() -> closeModal(overlay));
            overlay.setOnMouseClicked(ev -> {
                if (ev.getTarget() == overlay) closeModal(overlay);
            });
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    // Cerca lo StackPane overlay nella scena corrente.
    private StackPane findOverlay() {
        if (tileAccettate == null || tileAccettate.getScene() == null) return null;
        Node n = tileAccettate.getScene().lookup("#modalOverlay");
        if (n instanceof StackPane sp) return sp;
        return null;
    }

    // Chiude la modale overlay.
    private void closeModal(StackPane overlay) {
        if (overlay == null) return;
        overlay.getChildren().clear();
        overlay.setVisible(false);
        overlay.setManaged(false);
        overlay.setMouseTransparent(true);
    }
    private void handleModifica(offerta v, boolean resetStatoAttesa) {
        // Modifica l'offerta in base al tipo (scambio/vendita/regalo).
        annuncio a = v.getAnnuncio();
        if (a != null && a.getTipo() == tipoAnnuncio.scambio) {
            ScambioInput input = openScambioDialog(v.getIdOfferta());
            if (input == null) return;

            try (Connection conn = DB.getConnection()) {
                oggettoScambioDAO oggDao = new oggettoScambioDAO(conn);
                offertaDAO offDao = new offertaDAO(conn);
                ScambioImageHandler imgHandler = new ScambioImageHandler();

                conn.setAutoCommit(false);
                try {
                    if (resetStatoAttesa) {
                        offDao.aggiornaImportoEMessaggioEStato(v.getIdOfferta(), null, input.messaggio, statoOfferta.in_attesa);
                    } else {
                        offDao.aggiornaMessaggio(v.getIdOfferta(), input.messaggio);
                    }
                    oggDao.deleteByOfferta(v.getIdOfferta());
                    int ordine = 1;
                    for (OfferItem it : input.items) {
                        String path = imgHandler.saveImage(v.getIdOfferta(), it.imagePath, ordine++);
                        oggDao.insert(v.getIdOfferta(), it.name, path);
                    }
                    conn.commit();
                } catch (Exception ex) {
                    conn.rollback();
                    throw ex;
                } finally {
                    conn.setAutoCommit(true);
                }

                loadData();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            return;
        }

        if (a != null && a.getTipo() == tipoAnnuncio.vendita) {
            BigDecimal nuova = openVenditaDialog();
            if (nuova == null) return;
            try (Connection conn = DB.getConnection()) {
                offertaDAO offDao = new offertaDAO(conn);
                if (resetStatoAttesa) {
                    String msg = openMessaggioDialog(v.getMessaggio(), true);
                    offDao.aggiornaImportoEMessaggioEStato(v.getIdOfferta(), nuova, normalizeMessage(msg), statoOfferta.in_attesa);
                } else {
                    offDao.aggiornaImporto(v.getIdOfferta(), nuova);
                }
                loadData();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            return;
        }

        if (a != null && a.getTipo() == tipoAnnuncio.regalo) {
            String msg = openMessaggioDialog(v.getMessaggio(), false);
            if (msg == null || msg.isBlank()) return;
            try (Connection conn = DB.getConnection()) {
                offertaDAO offDao = new offertaDAO(conn);
                if (resetStatoAttesa) {
                    offDao.aggiornaImportoEMessaggioEStato(v.getIdOfferta(), null, msg, statoOfferta.in_attesa);
                } else {
                    offDao.aggiornaMessaggio(v.getIdOfferta(), msg);
                }
                loadData();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    // Aggiorna solo il messaggio dell'offerta (se consentito dallo stato).
    private void handleEditMessaggio(offerta v) {
        if (v.getStato() == statoOfferta.accettata || v.getStato() == statoOfferta.ritirata) return;
        String msg = openMessaggioDialog(v.getMessaggio(), false);
        if (msg == null || msg.isBlank()) return;
        try (Connection conn = DB.getConnection()) {
            offertaDAO offDao = new offertaDAO(conn);
            offDao.aggiornaMessaggio(v.getIdOfferta(), msg);
            loadData();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    // Invia la recensione per un'offerta accettata.
    private void handleRecensione(offerta v, offertaCardController.RecensioneInput input) {
        String recensore = Session.getMatricola();
        if (recensore == null || recensore.isBlank()) {
            new Alert(Alert.AlertType.WARNING, "Devi essere loggato per inviare una recensione.").showAndWait();
            return;
        }
        String matricolaVenditore = v.getAnnuncio() != null && v.getAnnuncio().getVenditore() != null
                ? v.getAnnuncio().getVenditore().getMatricola()
                : null;
        if (matricolaVenditore == null || matricolaVenditore.isBlank()) {
            new Alert(Alert.AlertType.ERROR, "Venditore non disponibile per questa offerta.").showAndWait();
            return;
        }

        try (Connection conn = DB.getConnection()) {
            recensioneDAO recDao = new recensioneDAO(conn);
            conn.setAutoCommit(false);
            try {
                recDao.deleteByOffertaAndRecensore(v.getIdOfferta(), recensore);
                recDao.inserisci(v.getIdOfferta(), recensore, matricolaVenditore, input.stelle, input.commento);
                conn.commit();
            } catch (Exception ex) {
                conn.rollback();
                throw ex;
            } finally {
                conn.setAutoCommit(true);
            }
            new Alert(Alert.AlertType.INFORMATION, "Recensione inviata").showAndWait();
        } catch (Exception ex) {
            ex.printStackTrace();
            new Alert(Alert.AlertType.ERROR, "Errore durante il salvataggio della recensione: " + ex.getMessage()).showAndWait();
        }
    }

    // Mostra gli oggetti allegati a un'offerta di scambio.
    private void openOggettiOfferti(int idOfferta) {
        try (Connection conn = DB.getConnection()) {
            oggettoScambioDAO dao = new oggettoScambioDAO(conn);
            var items = dao.getByOfferta(idOfferta);
            if (items.isEmpty()) {
                new Alert(Alert.AlertType.INFORMATION, "Nessun oggetto allegato all'offerta.").showAndWait();
                return;
            }
            Dialog<Void> dialog = new Dialog<>();
            dialog.setTitle("Oggetti offerti");
            dialog.setHeaderText(null);
            dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);

            VBox box = new VBox(10);
            box.setPadding(new Insets(10));

            for (oggettoScambio it : items) {
                HBox row = new HBox(10);
                row.setAlignment(Pos.CENTER_LEFT);
                ImageView iv = new ImageView();
                iv.setFitWidth(80);
                iv.setFitHeight(80);
                iv.setPreserveRatio(true);
                if (it.getPath() != null && !it.getPath().isBlank()) {
                    File f = new File(it.getPath());
                    if (!f.isAbsolute()) {
                        f = new File(System.getProperty("user.dir"), it.getPath());
                    }
                    if (f.exists()) {
                        iv.setImage(new Image(f.toURI().toString(), true));
                    }
                }
                Label name = new Label(it.getNomeOggetto() != null ? it.getNomeOggetto() : "(senza nome)");
                name.setStyle("-fx-font-weight: bold;");
                row.getChildren().addAll(iv, name);
                box.getChildren().add(row);
            }

            dialog.getDialogPane().setContent(box);
            dialog.showAndWait();
        } catch (Exception ex) {
            ex.printStackTrace();
            new Alert(Alert.AlertType.ERROR, "Errore nel caricamento degli oggetti di scambio").showAndWait();
        }
    }

    // Mostra il messaggio allegato a una offerta.
    private void openMessaggioOfferta(String messaggio) {
        String testo = (messaggio == null || messaggio.isBlank())
                ? "Nessun messaggio inserito."
                : messaggio.trim();
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Messaggio offerta");
        alert.setHeaderText(null);
        alert.setContentText(testo);
        alert.showAndWait();
    }

    // Crea una nuova offerta (scambio/vendita/regalo).
    private void handleNuovaOfferta(offerta v) {
        annuncio a = v.getAnnuncio();
        if (a != null && a.getTipo() == tipoAnnuncio.scambio) {
            ScambioInput input = openScambioDialog(v.getIdOfferta());
            if (input == null) return;
            try (Connection conn = DB.getConnection()) {
                offertaDAO offDao = new offertaDAO(conn);
                oggettoScambioDAO oggDao = new oggettoScambioDAO(conn);
                ScambioImageHandler imgHandler = new ScambioImageHandler();

                conn.setAutoCommit(false);
                try {
                    int newId = offDao.creaOffertaScambio(a.getIdAnnuncio(), Session.getMatricola(), input.messaggio);
                    int ordine = 1;
                    for (OfferItem it : input.items) {
                        String path = imgHandler.saveImage(newId, it.imagePath, ordine++);
                        oggDao.insert(newId, it.name, path);
                    }
                    conn.commit();
                } catch (Exception ex) {
                    conn.rollback();
                    throw ex;
                } finally {
                    conn.setAutoCommit(true);
                }
                loadData();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            return;
        }

        if (a != null && a.getTipo() == tipoAnnuncio.vendita) {
            BigDecimal nuova = openVenditaDialog();
            if (nuova == null) return;
            try (Connection conn = DB.getConnection()) {
                String sql = """
                    INSERT INTO offerta (id_annuncio, matricola_offerente, stato, importo_proposto, messaggio, data_offerta)
                    VALUES (?, ?, ?::stato_offerta_enum, ?, ?, now())
                """;
                try (var ps = conn.prepareStatement(sql)) {
                    ps.setInt(1, a.getIdAnnuncio());
                    ps.setString(2, Session.getMatricola());
                    ps.setString(3, statoOfferta.in_attesa.name());
                    ps.setBigDecimal(4, nuova);
                    ps.setNull(5, java.sql.Types.VARCHAR);
                    ps.executeUpdate();
                }
                loadData();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            return;
        }

        if (a != null && a.getTipo() == tipoAnnuncio.regalo) {
            String msg = openMessaggioDialog(null, false);
            if (msg == null || msg.isBlank()) return;
            try (Connection conn = DB.getConnection()) {
                String sql = """
                    INSERT INTO offerta (id_annuncio, matricola_offerente, stato, importo_proposto, messaggio, data_offerta)
                    VALUES (?, ?, ?::stato_offerta_enum, NULL, ?, now())
                """;
                try (var ps = conn.prepareStatement(sql)) {
                    ps.setInt(1, a.getIdAnnuncio());
                    ps.setString(2, Session.getMatricola());
                    ps.setString(3, statoOfferta.in_attesa.name());
                    ps.setString(4, msg.trim());
                    ps.executeUpdate();
                }
                loadData();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    // Dialog per inserire importo offerto.
    private BigDecimal openVenditaDialog() {
        TextInputDialog d = new TextInputDialog();
        d.setTitle("Nuova offerta");
        d.setHeaderText(null);
        d.setContentText("Inserisci il prezzo:");
        return d.showAndWait().map(s -> {
            try {
                return new BigDecimal(s.trim().replace(',', '.'));
            } catch (Exception ex) {
                return null;
            }
        }).orElse(null);
    }

    // Dialog per inserire o modificare il messaggio offerta.
    private String openMessaggioDialog(String current, boolean allowEmpty) {
        TextInputDialog d = new TextInputDialog(current != null ? current : "");
        d.setTitle("Messaggio offerta");
        d.setHeaderText(null);
        d.setContentText("Messaggio:");
        return d.showAndWait()
                .map(String::trim)
                .filter(s -> allowEmpty || !s.isBlank())
                .orElse(null);
    }

    // Normalizza un messaggio: null/blank -> null.
    private String normalizeMessage(String msg) {
        if (msg == null || msg.isBlank()) return null;
        return msg.trim();
    }

    // Dialog per gestire oggetti di scambio (lista + immagini).
    private ScambioInput openScambioDialog(int idOfferta) {
        Dialog<ScambioInput> dialog = new Dialog<>();
        dialog.setTitle("Offerta di scambio");
        dialog.setHeaderText(null);

        ButtonType ok = new ButtonType("Salva", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(ok, ButtonType.CANCEL);

        ListView<OfferItem> list = new ListView<>();
        list.setPrefHeight(140);

        TextField msgField = new TextField();
        msgField.setPromptText("Dettagli / condizioni (opzionale)");

        Button addBtn = new Button("+ Aggiungi oggetto");
        addBtn.setOnAction(e -> {
            OfferItem item = openItemDialog();
            if (item != null) list.getItems().add(item);
        });

        Button removeBtn = new Button("Rimuovi selezionato");
        removeBtn.setOnAction(e -> {
            OfferItem sel = list.getSelectionModel().getSelectedItem();
            if (sel != null) list.getItems().remove(sel);
        });

        HBox actions = new HBox(8, addBtn, removeBtn);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(10));
        grid.add(new Label("Oggetti:"), 0, 0);
        grid.add(list, 0, 1, 2, 1);
        grid.add(actions, 0, 2, 2, 1);
        grid.add(new Label("Nota:"), 0, 3);
        grid.add(msgField, 0, 4, 2, 1);

        if (idOfferta > 0) {
            try (Connection conn = DB.getConnection()) {
                oggettoScambioDAO oggDao = new oggettoScambioDAO(conn);
                for (oggettoScambio it : oggDao.getByOfferta(idOfferta)) {
                    if (it.getNomeOggetto() != null) {
                        String path = it.getPath();
                        if (path != null && !path.isBlank() && !new java.io.File(path).isAbsolute()) {
                            path = java.nio.file.Paths.get(System.getProperty("user.dir")).resolve(path).toString();
                        }
                        list.getItems().add(new OfferItem(it.getNomeOggetto(), path));
                    }
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }

        dialog.getDialogPane().setContent(grid);

        Node okButton = dialog.getDialogPane().lookupButton(ok);
        okButton.disableProperty().bind(Bindings.isEmpty(list.getItems()));

        dialog.setResultConverter(btn -> {
            if (btn != ok) return null;
            ScambioInput input = new ScambioInput();
            input.items = new ArrayList<>(list.getItems());
            input.messaggio = msgField.getText();
            return input;
        });

        return dialog.showAndWait().orElse(null);
    }

    // Dialog per aggiungere un singolo oggetto di scambio.
    private OfferItem openItemDialog() {
        Dialog<OfferItem> dialog = new Dialog<>();
        dialog.setTitle("Aggiungi oggetto");
        dialog.setHeaderText(null);

        ButtonType addType = new ButtonType("Aggiungi", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(addType, ButtonType.CANCEL);

        TextField nameField = new TextField();
        nameField.setPromptText("Nome oggetto");

        ImageView preview = new ImageView();
        preview.setFitWidth(80);
        preview.setFitHeight(80);
        preview.setPreserveRatio(true);

        Label imageLabel = new Label("Nessuna immagine selezionata");
        Button chooseButton = new Button("Seleziona immagine");

        FileChooser chooser = new FileChooser();
        chooser.setTitle("Seleziona immagine");
        chooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Immagini", "*.png", "*.jpg", "*.jpeg", "*.gif")
        );

        SimpleBooleanProperty imageSelected = new SimpleBooleanProperty(false);
        final String[] imagePath = new String[1];

        chooseButton.setOnAction(e -> {
            if (dialog.getDialogPane().getScene() == null || dialog.getDialogPane().getScene().getWindow() == null) {
                return;
            }
            java.io.File file = chooser.showOpenDialog(dialog.getDialogPane().getScene().getWindow());
            if (file != null) {
                imagePath[0] = file.getAbsolutePath();
                imageLabel.setText(file.getName());
                preview.setImage(new Image(file.toURI().toString(), true));
                imageSelected.set(true);
            }
        });

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(10));
        grid.add(new Label("Nome:"), 0, 0);
        grid.add(nameField, 1, 0);
        grid.add(new Label("Immagine:"), 0, 1);
        grid.add(chooseButton, 1, 1);
        grid.add(imageLabel, 1, 2);
        grid.add(preview, 1, 3);

        dialog.getDialogPane().setContent(grid);

        Node addButton = dialog.getDialogPane().lookupButton(addType);
        addButton.disableProperty().bind(
                nameField.textProperty().isEmpty().or(imageSelected.not())
        );

        dialog.setResultConverter(button -> {
            if (button != addType) return null;
            String name = nameField.getText().trim();
            if (name.isEmpty() || imagePath[0] == null) {
                return null;
            }
            return new OfferItem(name, imagePath[0]);
        });

        return dialog.showAndWait().orElse(null);
    }

    private static class OfferItem {
        private final String name;
        private final String imagePath;

        OfferItem(String name, String imagePath) {
            this.name = name;
            this.imagePath = imagePath;
        }

        @Override
        public String toString() {
            return name != null ? name : "";
        }
    }

    private static class ScambioInput {
        private List<OfferItem> items = new ArrayList<>();
        private String messaggio;
    }

    @FXML
    // Toggle visibilita' delle offerte ricevute in attesa.
    private void toggleRicevuteAttesa() { togglePane(tileRicevuteAttesa, btnToggleRicevuteAttesa); }
    @FXML
    // Toggle visibilita' delle offerte ricevute accettate.
    private void toggleRicevuteAccettate() { togglePane(tileRicevuteAccettate, btnToggleRicevuteAccettate); }
    @FXML
    // Toggle visibilita' delle offerte ricevute chiuse.
    private void toggleRicevuteChiuse() { togglePane(tileRicevuteChiuse, btnToggleRicevuteChiuse); }
    @FXML
    // Toggle visibilita' delle offerte accettate inviate.
    private void toggleAccettate() { togglePane(tileAccettate, btnToggleAccettate); }
    @FXML
    // Toggle visibilita' delle offerte inviate attive.
    private void toggleInviateAttive() { togglePane(tileInviateAttive, btnToggleInviateAttive); }
    @FXML
    // Toggle visibilita' delle offerte inviate rifiutate.
    private void toggleInviateRifiutate() { togglePane(tileInviateRifiutate, btnToggleInviateRifiutate); }

    // Utility: mostra/nasconde un TilePane e aggiorna l'icona del bottone.
    private void togglePane(TilePane pane, Button toggleBtn) {
        boolean newVis = !pane.isVisible();
        pane.setVisible(newVis);
        pane.setManaged(newVis);
        if (toggleBtn != null) {
            toggleBtn.setText(newVis ? "▾" : "▸");
        }
    }
}
