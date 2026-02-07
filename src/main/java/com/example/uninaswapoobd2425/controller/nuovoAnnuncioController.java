package com.example.uninaswapoobd2425.controller;

import com.example.uninaswapoobd2425.model.categoriaAnnuncio;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.TilePane;
import javafx.scene.shape.Rectangle;
import javafx.stage.FileChooser;
import javafx.stage.Window;
import com.example.uninaswapoobd2425.dao.DB;
import com.example.uninaswapoobd2425.dao.annuncioDAO;
import com.example.uninaswapoobd2425.dao.immagineAnnuncioDAO;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.file.*;
import java.sql.Connection;
import java.io.File;
import java.util.*;

public class nuovoAnnuncioController {
    private static final int MAX_IMG = 5;
    private static final double THUMB_SIZE = 90;

    @FXML private TilePane tilePreview;
    @FXML private Label lblCounter;
    @FXML private Button annullaButton;
    @FXML private TextField titoloField;
    @FXML private TextArea descrizioneArea;
    @FXML private TextField prezzoField;
    @FXML private ComboBox<categoriaAnnuncio> categoriaCombo;
    @FXML private ToggleButton btnVendita;
    @FXML private ToggleButton btnScambio;
    @FXML private ToggleButton btnRegalo;
    @FXML private Label euroLabel;
    // Seleziona tipo vendita.
    @FXML private void onTipoVendita() { setTipo("vendita"); }
    // Seleziona tipo scambio.
    @FXML private void onTipoScambio() { setTipo("scambio"); }
    // Seleziona tipo regalo.
    @FXML private void onTipoRegalo()  { setTipo("regalo"); }

    private final ToggleGroup tipoGroup = new ToggleGroup();
    private String tipo = "vendita";

    private final Map<categoriaAnnuncio, String> labelMap = new EnumMap<>(categoriaAnnuncio.class);
    private final Map<categoriaAnnuncio, String> iconMap  = new EnumMap<>(categoriaAnnuncio.class);

    private final List<File> selectedImages = new ArrayList<>();

    @FXML
    // Inizializza toggle tipo, combo categoria e contatore immagini.
    private void initialize() {
        updateCounter();

        btnVendita.setToggleGroup(tipoGroup);
        btnScambio.setToggleGroup(tipoGroup);
        btnRegalo.setToggleGroup(tipoGroup);

        btnVendita.setGraphic(icon("/com/example/uninaswapoobd2425/imgs/vendita.png"));
        btnScambio.setGraphic(icon("/com/example/uninaswapoobd2425/imgs/scambio.png"));
        btnRegalo.setGraphic(icon("/com/example/uninaswapoobd2425/imgs/regalo.png"));

        btnVendita.setSelected(true);
        setTipo("vendita");

        tipoGroup.selectedToggleProperty().addListener((obs, oldT, newT) -> {
            if (newT == btnVendita) setTipo("vendita");
            else if (newT == btnScambio) setTipo("scambio");
            else if (newT == btnRegalo) setTipo("regalo");
        });
        labelMap.put(categoriaAnnuncio.libri, "Libri");
        labelMap.put(categoriaAnnuncio.informatica, "Informatica");
        labelMap.put(categoriaAnnuncio.abbigliamento, "Abbigliamento");
        labelMap.put(categoriaAnnuncio.strumenti_musicali, "Strumenti musicali");
        labelMap.put(categoriaAnnuncio.altro, "Altro");

        iconMap.put(categoriaAnnuncio.libri, "/com/example/uninaswapoobd2425/imgs/libri.png");
        iconMap.put(categoriaAnnuncio.informatica, "/com/example/uninaswapoobd2425/imgs/informatica.png");
        iconMap.put(categoriaAnnuncio.abbigliamento, "/com/example/uninaswapoobd2425/imgs/abbigliamento.png");
        iconMap.put(categoriaAnnuncio.strumenti_musicali, "/com/example/uninaswapoobd2425/imgs/strumenti_musicali.png");
        iconMap.put(categoriaAnnuncio.altro, "/com/example/uninaswapoobd2425/imgs/altro.png");

        categoriaCombo.setItems(FXCollections.observableArrayList(categoriaAnnuncio.values()));
        categoriaCombo.getSelectionModel().select(categoriaAnnuncio.libri);

        categoriaCombo.setCellFactory(cb -> new CategoriaCell());
        categoriaCombo.setButtonCell(new CategoriaCell());

        titoloField.setTextFormatter(new TextFormatter<>(change -> {
            String text = change.getControlNewText();
            return text.length() <= 25 ? change : null;
        }));
    }

    private class CategoriaCell extends ListCell<categoriaAnnuncio> {
        @Override
        // Renderizza una voce categoria con icona e testo.
        protected void updateItem(categoriaAnnuncio item, boolean empty) {
            super.updateItem(item, empty);

            if (empty || item == null) {
                setText(null);
                setGraphic(null);
                return;
            }

            String label = labelMap.getOrDefault(item, item.name());
            String iconPath = iconMap.get(item);

            ImageView icon = new ImageView();
            icon.setFitWidth(25);
            icon.setFitHeight(25);
            icon.setPreserveRatio(true);

            if (iconPath != null) {
                var is = getClass().getResourceAsStream(iconPath);
                if (is != null) {
                    icon.setImage(new Image(is));
                } // se is==null, niente icona e non crasha
            }

            Label text = new Label(label);
            text.getStyleClass().add("combo-text");
            HBox row = new HBox(8, icon, text);
            row.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
            setText(null);
            setGraphic(row);
        }

    }
    // Per i bottoni vendita,scambio,regalo

    // Imposta il tipo annuncio e abilita/disabilita il prezzo.
    private void setTipo(String newTipo) {
        this.tipo = newTipo;

        boolean enablePrezzo = "vendita".equalsIgnoreCase(newTipo);
        prezzoField.setDisable(!enablePrezzo);
        euroLabel.setDisable(!enablePrezzo);
        if (!enablePrezzo) {

            prezzoField.clear();
        }
    }

    // Crea un'icona grafica da resource.
    private javafx.scene.Node icon(String path) {
        var is = getClass().getResourceAsStream(path);
        if (is == null) return null;
        var img = new javafx.scene.image.Image(is);
        var iv = new javafx.scene.image.ImageView(img);
        iv.setFitWidth(16);
        iv.setFitHeight(16);
        iv.setPreserveRatio(true);
        return iv;
    }

    @FXML
    // Apre il file chooser e carica le immagini selezionate.
    private void onCaricaImmagini() {
        if (selectedImages.size() >= MAX_IMG) {
            showWarn("Limite raggiunto", "Puoi caricare al massimo " + MAX_IMG + " immagini.");
            return;
        }

        FileChooser fc = new FileChooser();
        fc.setTitle("Seleziona immagini");
        fc.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Immagini", "*.png", "*.jpg", "*.jpeg")
        );

        Window w = tilePreview.getScene().getWindow();
        List<File> files = fc.showOpenMultipleDialog(w);
        if (files == null || files.isEmpty()) return;

        for (File f : files) {
            if (selectedImages.size() >= MAX_IMG) break;

            // evita duplicati
            boolean already = selectedImages.stream().anyMatch(x -> x.getAbsolutePath().equals(f.getAbsolutePath()));
            if (already) continue;

            addImage(f);
        }

        updateCounter();
    }

    // Aggiunge una miniatura e la collega alla lista selezionata.
    private void addImage(File file) {
        Image img = new Image(file.toURI().toString(), THUMB_SIZE, THUMB_SIZE, true, true);

        ImageView iv = new ImageView(img);
        iv.setFitWidth(THUMB_SIZE);
        iv.setFitHeight(THUMB_SIZE);
        iv.setPreserveRatio(true);

        // Clip arrotondato
        Rectangle clip = new Rectangle(THUMB_SIZE, THUMB_SIZE);
        clip.setArcWidth(12);
        clip.setArcHeight(12);
        iv.setClip(clip);

        // Bottone X per rimuovere
        Button btnRemove = new Button("✕");
        btnRemove.setFocusTraversable(false);
        btnRemove.setStyle("""
            -fx-background-radius: 999;
            -fx-background-color: rgba(0,0,0,0.55);
            -fx-text-fill: white;
            -fx-font-weight: bold;
            -fx-padding: 2 7 2 7;
        """);

        // posiziona in alto a destra dentro lo StackPane
        StackPane.setAlignment(btnRemove, javafx.geometry.Pos.TOP_RIGHT);

        StackPane thumb = new StackPane(iv, btnRemove);
        thumb.setStyle("""
            -fx-background-color: rgba(255,255,255,0.6);
            -fx-background-radius: 12;
            -fx-padding: 6;
        """);

        // click destro sulla miniatura per rimuovere
        thumb.setOnMouseClicked(e -> {
            if (e.getButton() == MouseButton.SECONDARY) {
                removeImage(file, thumb);
            }
        });

        btnRemove.setOnAction(e -> removeImage(file, thumb));

        selectedImages.add(file);
        tilePreview.getChildren().add(thumb);
    }

    // Rimuove una immagine selezionata e la miniatura.
    private void removeImage(File file, StackPane thumbNode) {
        selectedImages.removeIf(f -> f.getAbsolutePath().equals(file.getAbsolutePath()));
        tilePreview.getChildren().remove(thumbNode);
        updateCounter();
    }

    // Aggiorna il contatore immagini.
    private void updateCounter() {
        lblCounter.setText(selectedImages.size() + "/" + MAX_IMG);
    }

    // Ritorna una copia immutabile delle immagini selezionate.
    public List<File> getSelectedImages() {
        return List.copyOf(selectedImages);
    }

    // Mostra un alert di warning.
    private void showWarn(String title, String msg) {
        Alert a = new Alert(Alert.AlertType.WARNING);
        a.setTitle(title);
        a.setHeaderText(null);
        a.setContentText(msg);
        a.showAndWait();
    }
    @FXML
    // Chiude la modale annulla.
    void handleAnnulla(ActionEvent event) {
        // Trucco per nascondere il genitore (l'overlay) senza complicati passaggi di dati
        var overlay = annullaButton.getScene().lookup("#modalOverlay");
        if (overlay != null) {
            overlay.setVisible(false);
            overlay.setManaged(false);
            overlay.setMouseTransparent(true);
            if (overlay instanceof StackPane sp) {
                sp.getChildren().clear();
            }
        }

        // OPPURE si risale alla gerarchia
        // ((StackPane) annullaButton.getParent().getParent()).setVisible(false);
    }
    @FXML
    // Valida input, inserisce annuncio e salva immagini.
    private void handlePubblicaAnnuncio(ActionEvent event) {

        String titolo = safe(titoloField.getText());
        String descr = safe(descrizioneArea.getText());

        if (titolo.isEmpty()) { showWarn("Errore", "Inserisci il titolo."); return; }
        if (descr.isEmpty())  { showWarn("Errore", "Inserisci la descrizione."); return; }

        categoriaAnnuncio cat = categoriaCombo.getValue();
        if (cat == null) { showWarn("Errore", "Seleziona la categoria."); return; }

        String categoria = cat.name();
        String stato = "attivo";

        String tipoSelezionato = this.tipo;
        String matricolaVenditore = getMatricolaUtenteLoggato();


        BigDecimal prezzo = parsePrezzoBigDecimalOrNull(prezzoField.getText());
        if (!"vendita".equalsIgnoreCase(tipo)) prezzo = null;

        if ("vendita".equalsIgnoreCase(tipo) && prezzo == null) {
            showWarn("Errore", "Inserisci un prezzo per la vendita.");
            return;
        }

        try (Connection conn = DB.getConnection()) {
            boolean oldAuto = conn.getAutoCommit();
            conn.setAutoCommit(false);

            try {
                annuncioDAO aDao = new annuncioDAO(conn);
                int idAnnuncio = aDao.insertAnnuncioReturningId(
                        titolo,
                        descr,
                        prezzo,
                        new java.sql.Date(System.currentTimeMillis()),
                        matricolaVenditore,
                        categoria,
                        stato,
                        tipoSelezionato
                );

                ImageHandler handler = new ImageHandler();
                var saved = handler.saveImages(idAnnuncio, selectedImages);

                immagineAnnuncioDAO iDao = new immagineAnnuncioDAO(conn);
                iDao.insertImages(idAnnuncio, saved);

                conn.commit();
                conn.setAutoCommit(oldAuto);

                new Alert(Alert.AlertType.INFORMATION, "Annuncio pubblicato! ID: " + idAnnuncio).showAndWait();
                var overlay = annullaButton.getScene().lookup("#modalOverlay");
                if (overlay != null) {
                    overlay.setVisible(false);
                    overlay.setManaged(false);
                    overlay.setMouseTransparent(true);
                    if (overlay instanceof StackPane sp) {
                        sp.getChildren().clear();
                    }
                }

            } catch (Exception e) {
                conn.rollback();
                conn.setAutoCommit(oldAuto);
                showWarn("Errore pubblicazione", e.getMessage());
            }

        } catch (Exception e) {
            showWarn("Errore DB", e.getMessage());
        }
    }

    // Converte il testo del prezzo in BigDecimal validato.
    private BigDecimal parsePrezzoBigDecimalOrNull(String s) {
        if (s == null) return null;
        String t = s.trim();
        if (t.isEmpty()) return null;

        try {
            t = t.replace(",", ".");
            BigDecimal bd = new BigDecimal(t);

            // forza scala 2 (NUMERIC(8,2))
            bd = bd.setScale(2, RoundingMode.HALF_UP);

            // opzionale: limite massimo (NUMERIC(8,2) => max 999999.99)
            if (bd.compareTo(new BigDecimal("999999.99")) > 0) {
                showWarn("Prezzo non valido", "Il prezzo massimo è 999999.99");
                return null;
            }
            if (bd.compareTo(BigDecimal.ZERO) < 0) {
                showWarn("Prezzo non valido", "Il prezzo non può essere negativo");
                return null;
            }

            return bd;
        } catch (NumberFormatException ex) {
            showWarn("Prezzo non valido", "Inserisci un numero (es. 10 oppure 10,50).");
            return null;
        }
    }

    // Normalizza una stringa null-safe.
    private String safe(String s) { return s == null ? "" : s.trim(); }

    // Recupera la matricola dell'utente loggato dalla sessione.
    private String getMatricolaUtenteLoggato() {
        return com.example.uninaswapoobd2425.model.Session.getMatricola();
    }




}
