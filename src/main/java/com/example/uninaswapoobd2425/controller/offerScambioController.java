package com.example.uninaswapoobd2425.controller;

import com.example.uninaswapoobd2425.dao.DB;
import com.example.uninaswapoobd2425.model.annuncio;
import com.example.uninaswapoobd2425.model.Session;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.stage.FileChooser;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

public class offerScambioController implements offerPaneController {

    @FXML private ListView<OfferItem> listOggetti;
    @FXML private TextField txtDettagli;

    private annuncio ann;

    @Override
    // Inizializza la lista oggetti con cella custom.
    public void init(annuncio a) {
        this.ann = a;
        listOggetti.setItems(FXCollections.observableArrayList());
        listOggetti.setCellFactory(lv -> new ListCell<>() {
            private final ImageView imageView = new ImageView();
            private final Label nameLabel = new Label();
            private final HBox content = new HBox(10, imageView, nameLabel);

            {
                imageView.setFitWidth(48);
                imageView.setFitHeight(48);
                imageView.setPreserveRatio(true);
                content.setAlignment(Pos.CENTER_LEFT);
            }

            @Override
            protected void updateItem(OfferItem item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                    return;
                }
                nameLabel.setText(item.getName());
                imageView.setImage(new Image("file:" + item.getImagePath(), true));
                setGraphic(content);
            }
        });
    }

    @FXML
    // Apre un dialog per aggiungere un oggetto allo scambio.
    private void handleAdd() {
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
            if (listOggetti.getScene() == null || listOggetti.getScene().getWindow() == null) {
                return;
            }
            java.io.File file = chooser.showOpenDialog(listOggetti.getScene().getWindow());
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
            if (button != addType) {
                return null;
            }
            String name = nameField.getText().trim();
            if (name.isEmpty() || imagePath[0] == null) {
                return null;
            }
            return new OfferItem(name, imagePath[0]);
        });

        dialog.showAndWait().ifPresent(item -> listOggetti.getItems().add(item));
    }

    @FXML
    // Valida e invia la proposta di scambio con oggetti e messaggio.
    private void handleInvia() {
        if (listOggetti.getItems().isEmpty()) {
            new Alert(Alert.AlertType.WARNING, "Inserisci almeno un oggetto da offrire.").showAndWait();
            return;
        }

        String matricolaOfferente = Session.getMatricola();
        if (matricolaOfferente == null || matricolaOfferente.isBlank()) {
            new Alert(Alert.AlertType.WARNING, "Devi essere loggato per inviare un'offerta.").showAndWait();
            return;
        }

        try (Connection conn = DB.getConnection()) {
            if (isSelfOffer(conn, matricolaOfferente)) {
                new Alert(Alert.AlertType.WARNING, "Non puoi fare un'offerta al tuo annuncio.").showAndWait();
                return;
            }
            if (hasActiveOffer(conn, matricolaOfferente)) {
                new Alert(Alert.AlertType.WARNING, "Hai gia inviato un'offerta per questo annuncio.").showAndWait();
                return;
            }
            conn.setAutoCommit(false);

            try {
                int idOfferta = insertOfferta(conn, matricolaOfferente);
                ScambioImageHandler imageHandler = new ScambioImageHandler();

                int ordine = 1;
                for (OfferItem item : listOggetti.getItems()) {
                    String dbPath = imageHandler.saveImage(idOfferta, item.getImagePath(), ordine++);
                    insertOggettoScambio(conn, idOfferta, item.getName(), dbPath);
                }

                conn.commit();
                new Alert(Alert.AlertType.INFORMATION, "Proposta di scambio inviata!").showAndWait();
            } catch (Exception ex) {
                conn.rollback();
                throw ex;
            }
        } catch (Exception ex) {
            new Alert(Alert.AlertType.ERROR, "Errore durante l'invio della proposta.").showAndWait();
            ex.printStackTrace();
        }
    }

    // Inserisce l'offerta scambio e restituisce l'id.
    private int insertOfferta(Connection conn, String matricolaOfferente) throws SQLException {
        String sql = """
            INSERT INTO offerta (id_annuncio, matricola_offerente, stato, importo_proposto, messaggio)
            VALUES (?, ?, ?::stato_offerta_enum, ?, ?)
            RETURNING id_offerta
        """;

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, ann.getIdAnnuncio());
            ps.setString(2, matricolaOfferente);
            ps.setString(3, "in_attesa");
            ps.setNull(4, Types.NUMERIC);

            String msg = txtDettagli.getText();
            if (msg == null || msg.isBlank()) ps.setNull(5, Types.VARCHAR);
            else ps.setString(5, msg.trim());

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        }

        throw new SQLException("Impossibile ottenere id_offerta (RETURNING vuoto)");
    }

    // Verifica se esiste gia' un'offerta attiva per questo annuncio.
    private boolean hasActiveOffer(Connection conn, String matricola) throws SQLException {
        String sql = """
            SELECT 1
            FROM offerta
            WHERE id_annuncio = ?
              AND matricola_offerente = ?
              AND stato NOT IN ('rifiutata'::stato_offerta_enum, 'ritirata'::stato_offerta_enum)
            LIMIT 1
        """;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, ann.getIdAnnuncio());
            ps.setString(2, matricola);
            return ps.executeQuery().next();
        }
    }

    // Impedisce offerte verso il proprio annuncio.
    private boolean isSelfOffer(Connection conn, String matricola) throws SQLException {
        String sql = "SELECT matricola_venditore FROM annuncio WHERE id_annuncio = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, ann.getIdAnnuncio());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String venditore = rs.getString(1);
                    return matricola.equalsIgnoreCase(venditore);
                }
            }
        }
        return false;
    }

    // Inserisce un oggetto di scambio associato all'offerta.
    private void insertOggettoScambio(Connection conn, int idOfferta, String nomeOggetto, String path) throws SQLException {
        String sql = """
            INSERT INTO oggetto_scambio (id_offerta, nome_oggetto, path)
            VALUES (?, ?, ?)
        """;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idOfferta);
            ps.setString(2, nomeOggetto);
            ps.setString(3, path);
            ps.executeUpdate();
        }
    }

    public static class OfferItem {
        private final String name;
        private final String imagePath;

        // Costruisce un item di scambio con nome e immagine.
        public OfferItem(String name, String imagePath) {
            this.name = name;
            this.imagePath = imagePath;
        }

        // Restituisce il nome dell'oggetto.
        public String getName() {
            return name;
        }

        // Restituisce il path immagine selezionata.
        public String getImagePath() {
            return imagePath;
        }
    }
}
