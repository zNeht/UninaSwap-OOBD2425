package com.example.uninaswapoobd2425.controller;

import com.example.uninaswapoobd2425.dao.DB;
import com.example.uninaswapoobd2425.dao.offertaDAO;
import com.example.uninaswapoobd2425.dao.oggettoScambioDAO;
import com.example.uninaswapoobd2425.dao.recensioneDAO;
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
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;

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

    public void loadData() {
        String matricola = Session.getMatricola();
        if (matricola == null || matricola.isBlank()) {
            return;
        }

        try (Connection conn = DB.getConnection()) {
            offertaDAO dao = new offertaDAO(conn);
            List<offertaDAO.OfferView> ricevute = dao.getOfferteRicevute(matricola);
            List<offertaDAO.OfferView> inviate = dao.getOfferteInviate(matricola);

            renderRicevute(ricevute);
            renderInviate(inviate);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void renderRicevute(List<offertaDAO.OfferView> list) throws Exception {
        tileRicevuteAttesa.getChildren().clear();
        tileRicevuteAccettate.getChildren().clear();
        tileRicevuteChiuse.getChildren().clear();

        for (offertaDAO.OfferView v : list) {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(
                    "/com/example/uninaswapoobd2425/offertaCard.fxml"
            ));
            Node card = loader.load();
            offertaCardController c = loader.getController();
            c.setData(v);
            c.setActionsVisible(true);

            boolean inAttesa = v.stato == statoOfferta.in_attesa;
            boolean annuncioBloccato = v.statoAnnuncio == statoAnnuncio.concluso
                    || v.statoAnnuncio == statoAnnuncio.annullato;

            // per ricevute: uso i bottoni come Accetta (modifica) / Rifiuta (annulla)
            c.setModificaLabel("Accetta");
            c.setRitiraLabel("Rifiuta offerta");
            c.setHandlers(
                    () -> handleAccetta(v),
                    () -> handleRifiuta(v)
            );
            c.setActionStates(inAttesa && !annuncioBloccato, inAttesa && !annuncioBloccato);
            if (v.tipo == tipoAnnuncio.scambio) {
                c.showItemsButton(true);
                c.setOnViewItems(() -> openOggettiOfferti(v.idOfferta));
            } else {
                c.showItemsButton(false);
            }

            if (v.stato == statoOfferta.accettata) {
                tileRicevuteAccettate.getChildren().add(card);
            } else if (inAttesa) {
                tileRicevuteAttesa.getChildren().add(card);
            } else {
                tileRicevuteChiuse.getChildren().add(card);
            }
        }
    }

    private void renderInviate(List<offertaDAO.OfferView> list) throws Exception {
        tileAccettate.getChildren().clear();
        tileInviateAttive.getChildren().clear();
        tileInviateRifiutate.getChildren().clear();

        String matricola = Session.getMatricola();

        for (offertaDAO.OfferView v : list) {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(
                    "/com/example/uninaswapoobd2425/offertaCard.fxml"
            ));
            Node card = loader.load();
            offertaCardController c = loader.getController();
            c.setData(v);
            c.showReviewButton(false);
            c.showModifyWithdraw(true);

            boolean inAttesa = v.stato == statoOfferta.in_attesa;
            boolean rifiutata = v.stato == statoOfferta.rifiutata;
            boolean ritirata = v.stato == statoOfferta.ritirata;
            boolean annuncioBloccato = v.statoAnnuncio == statoAnnuncio.concluso
                    || v.statoAnnuncio == statoAnnuncio.annullato;

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

            if (v.stato == statoOfferta.accettata) {
                boolean alreadyReviewed = matricola != null && !matricola.isBlank()
                        && hasAlreadyReviewed(v.idOfferta, matricola);
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

    private boolean hasAlreadyReviewed(int idOfferta, String matricolaRecensore) {
        try (Connection conn = DB.getConnection()) {
            recensioneDAO dao = new recensioneDAO(conn);
            return dao.existsByOffertaAndRecensore(idOfferta, matricolaRecensore);
        } catch (Exception ex) {
            return false;
        }
    }

    private void handleRitira(offertaDAO.OfferView v) {
        try (Connection conn = DB.getConnection()) {
            offertaDAO dao = new offertaDAO(conn);
            dao.aggiornaStato(v.idOfferta, statoOfferta.ritirata);
            loadData();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void handleRifiuta(offertaDAO.OfferView v) {
        try (Connection conn = DB.getConnection()) {
            offertaDAO dao = new offertaDAO(conn);
            dao.aggiornaStato(v.idOfferta, statoOfferta.rifiutata);
            loadData();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void handleAccetta(offertaDAO.OfferView v) {
        try (Connection conn = DB.getConnection()) {
            conn.setAutoCommit(false);
            try {
                offertaDAO dao = new offertaDAO(conn);
                dao.aggiornaStato(v.idOfferta, statoOfferta.accettata);
                dao.rifiutaAltreOfferteInAttesa(v.idAnnuncio, v.idOfferta);
                try (var ps = conn.prepareStatement("UPDATE annuncio SET stato = 'concluso'::stato_annuncio_enum WHERE id_annuncio = ?")) {
                    ps.setInt(1, v.idAnnuncio);
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
                    ps.setInt(1, v.idOfferta);
                    ps.setInt(2, v.idAnnuncio);
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
    private void handleModifica(offertaDAO.OfferView v, boolean resetStatoAttesa) {
        if (v.tipo == tipoAnnuncio.scambio) {
            ScambioInput input = openScambioDialog(v.idOfferta);
            if (input == null) return;

            try (Connection conn = DB.getConnection()) {
                oggettoScambioDAO oggDao = new oggettoScambioDAO(conn);
                offertaDAO offDao = new offertaDAO(conn);
                ScambioImageHandler imgHandler = new ScambioImageHandler();

                conn.setAutoCommit(false);
                try {
                    if (resetStatoAttesa) {
                        offDao.aggiornaImportoEMessaggioEStato(v.idOfferta, null, input.messaggio, statoOfferta.in_attesa);
                    } else {
                        offDao.aggiornaMessaggio(v.idOfferta, input.messaggio);
                    }
                    oggDao.deleteByOfferta(v.idOfferta);
                    int ordine = 1;
                    for (OfferItem it : input.items) {
                        String path = imgHandler.saveImage(v.idOfferta, it.imagePath, ordine++);
                        oggDao.insert(v.idOfferta, it.name, path);
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

        if (v.tipo == tipoAnnuncio.vendita) {
            BigDecimal nuova = openVenditaDialog();
            if (nuova == null) return;
            try (Connection conn = DB.getConnection()) {
                offertaDAO offDao = new offertaDAO(conn);
                if (resetStatoAttesa) {
                    offDao.aggiornaImportoEMessaggioEStato(v.idOfferta, nuova, null, statoOfferta.in_attesa);
                } else {
                    offDao.aggiornaImporto(v.idOfferta, nuova);
                }
                loadData();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            return;
        }

        if (v.tipo == tipoAnnuncio.regalo) {
            String msg = openMessaggioDialog();
            if (msg == null || msg.isBlank()) return;
            try (Connection conn = DB.getConnection()) {
                offertaDAO offDao = new offertaDAO(conn);
                if (resetStatoAttesa) {
                    offDao.aggiornaImportoEMessaggioEStato(v.idOfferta, null, msg, statoOfferta.in_attesa);
                } else {
                    offDao.aggiornaMessaggio(v.idOfferta, msg);
                }
                loadData();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    private void handleRecensione(offertaDAO.OfferView v, offertaCardController.RecensioneInput input) {
        String recensore = Session.getMatricola();
        if (recensore == null || recensore.isBlank()) {
            new Alert(Alert.AlertType.WARNING, "Devi essere loggato per inviare una recensione.").showAndWait();
            return;
        }
        if (v.matricolaVenditore == null || v.matricolaVenditore.isBlank()) {
            new Alert(Alert.AlertType.ERROR, "Venditore non disponibile per questa offerta.").showAndWait();
            return;
        }

        try (Connection conn = DB.getConnection()) {
            recensioneDAO recDao = new recensioneDAO(conn);
            conn.setAutoCommit(false);
            try {
                recDao.deleteByOffertaAndRecensore(v.idOfferta, recensore);
                recDao.inserisci(v.idOfferta, recensore, v.matricolaVenditore, input.stelle, input.commento);
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

            for (var it : items) {
                HBox row = new HBox(10);
                row.setAlignment(Pos.CENTER_LEFT);
                ImageView iv = new ImageView();
                iv.setFitWidth(80);
                iv.setFitHeight(80);
                iv.setPreserveRatio(true);
                if (it.path != null && !it.path.isBlank()) {
                    File f = new File(it.path);
                    if (!f.isAbsolute()) {
                        f = new File(System.getProperty("user.dir"), it.path);
                    }
                    if (f.exists()) {
                        iv.setImage(new Image(f.toURI().toString(), true));
                    }
                }
                Label name = new Label(it.nome != null ? it.nome : "(senza nome)");
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

    private void handleNuovaOfferta(offertaDAO.OfferView v) {
        if (v.tipo == tipoAnnuncio.scambio) {
            ScambioInput input = openScambioDialog(v.idOfferta);
            if (input == null) return;
            try (Connection conn = DB.getConnection()) {
                offertaDAO offDao = new offertaDAO(conn);
                oggettoScambioDAO oggDao = new oggettoScambioDAO(conn);
                ScambioImageHandler imgHandler = new ScambioImageHandler();

                conn.setAutoCommit(false);
                try {
                    int newId = offDao.creaOffertaScambio(v.idAnnuncio, Session.getMatricola(), input.messaggio);
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

        if (v.tipo == tipoAnnuncio.vendita) {
            BigDecimal nuova = openVenditaDialog();
            if (nuova == null) return;
            try (Connection conn = DB.getConnection()) {
                String sql = """
                    INSERT INTO offerta (id_annuncio, matricola_offerente, stato, importo_proposto, messaggio, data_offerta)
                    VALUES (?, ?, ?::stato_offerta_enum, ?, ?, now())
                """;
                try (var ps = conn.prepareStatement(sql)) {
                    ps.setInt(1, v.idAnnuncio);
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

        if (v.tipo == tipoAnnuncio.regalo) {
            String msg = openMessaggioDialog();
            if (msg == null || msg.isBlank()) return;
            try (Connection conn = DB.getConnection()) {
                String sql = """
                    INSERT INTO offerta (id_annuncio, matricola_offerente, stato, importo_proposto, messaggio, data_offerta)
                    VALUES (?, ?, ?::stato_offerta_enum, NULL, ?, now())
                """;
                try (var ps = conn.prepareStatement(sql)) {
                    ps.setInt(1, v.idAnnuncio);
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

    private String openMessaggioDialog() {
        TextInputDialog d = new TextInputDialog();
        d.setTitle("Nuova richiesta");
        d.setHeaderText(null);
        d.setContentText("Messaggio:");
        return d.showAndWait().map(String::trim).orElse("");
    }

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
                for (oggettoScambioDAO.ScambioItem it : oggDao.getByOfferta(idOfferta)) {
                    if (it.nome != null) {
                        String path = it.path;
                        if (path != null && !path.isBlank() && !new java.io.File(path).isAbsolute()) {
                            path = java.nio.file.Paths.get(System.getProperty("user.dir")).resolve(path).toString();
                        }
                        list.getItems().add(new OfferItem(it.nome, path));
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
    private void toggleRicevuteAttesa() { togglePane(tileRicevuteAttesa, btnToggleRicevuteAttesa); }
    @FXML
    private void toggleRicevuteAccettate() { togglePane(tileRicevuteAccettate, btnToggleRicevuteAccettate); }
    @FXML
    private void toggleRicevuteChiuse() { togglePane(tileRicevuteChiuse, btnToggleRicevuteChiuse); }
    @FXML
    private void toggleAccettate() { togglePane(tileAccettate, btnToggleAccettate); }
    @FXML
    private void toggleInviateAttive() { togglePane(tileInviateAttive, btnToggleInviateAttive); }
    @FXML
    private void toggleInviateRifiutate() { togglePane(tileInviateRifiutate, btnToggleInviateRifiutate); }

    private void togglePane(TilePane pane, Button toggleBtn) {
        boolean newVis = !pane.isVisible();
        pane.setVisible(newVis);
        pane.setManaged(newVis);
        if (toggleBtn != null) {
            toggleBtn.setText(newVis ? "▾" : "▸");
        }
    }
}
