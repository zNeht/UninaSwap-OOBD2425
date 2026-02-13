package com.example.uninaswapoobd2425.controller;

import com.example.uninaswapoobd2425.dao.DB;
import com.example.uninaswapoobd2425.dao.immagineAnnuncioDAO;
import com.example.uninaswapoobd2425.dao.oggettoScambioDAO;
import com.example.uninaswapoobd2425.model.Session;
import com.example.uninaswapoobd2425.model.annuncio;
import com.example.uninaswapoobd2425.model.immagineAnnuncio;
import com.example.uninaswapoobd2425.model.offerta;
import com.example.uninaswapoobd2425.model.oggettoScambio;
import com.example.uninaswapoobd2425.model.tipoAnnuncio;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Rectangle;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.math.BigDecimal;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class dettaglioAnnuncioController {
    @FXML private StackPane dialogRoot;
    @FXML private VBox detailCard;
    @FXML private ImageView imgMain;
    @FXML private Button btnPrevImg;
    @FXML private Button btnNextImg;
    @FXML private Label lblTipoBadge;
    @FXML private Label lblTitolo;
    @FXML private Label lblPrezzo;
    @FXML private Label lblCategoria;
    @FXML private Label lblDescrizione;
    @FXML private Label lblLuogo;
    @FXML private Label lblVenditore;
    @FXML private Label lblVenditoreEmail;
    @FXML private Button btnDeleteAnnuncio;

    @FXML private VBox offerActionBox;
    @FXML private VBox offerContainer;
    @FXML private VBox offerDetailBox;
    @FXML private VBox offerDetailContent;

    private final List<immagineAnnuncio> immagini = new ArrayList<>();
    private int currentImageIndex = 0;
    private annuncio currentAnnuncio;
    private Image fallbackImage;
    private Runnable onClose = () -> {};
    private Runnable onDeleted = () -> {};

    //Bind immagine - di 28 px e nascondo le freccie per cambiare immagine
    @FXML
    private void initialize() {
        imgMain.fitWidthProperty().bind(dialogRoot.widthProperty().subtract(28));
        clipToCardBounds();
        roundImage(imgMain, 18);
        toggleArrows(false);
    }

    // Imposta la callback di chiusura per la modale.
    public void setOnClose(Runnable r) { this.onClose = r; }
    // Imposta la callback dopo eliminazione annuncio.
    public void setOnDeleted(Runnable r) { this.onDeleted = r; }

    @FXML
    // Chiude la modale tramite callback.
    private void handleClose() { onClose.run(); }
    //Riempie i dettagli dell annuncio
    public void setAnnuncio(annuncio a) {
        this.currentAnnuncio = a;
        lblTitolo.setText(a.getTitolo());
        lblDescrizione.setText(a.getDescrizione());

        // categoria con prima lettera maiuscola
        lblCategoria.setText(capFirst(a.getCategoria().name()));

        // badge tipo
        lblTipoBadge.setText(labelTipo(a.getTipo()));

        String email = a.getVenditore() != null ? a.getVenditore().getMail() : null;
        lblVenditoreEmail.setText(email != null ? email : "-");

        // prezzo
        if (a.getTipo() == tipoAnnuncio.vendita) {
            lblPrezzo.setText("€ " + a.getPrezzo().toString());
        } else if (a.getTipo() == tipoAnnuncio.regalo) {
            lblPrezzo.setText("Regalo");
        } else {
            lblPrezzo.setText("Scambio");
        }

        loadImages(a);
        loadOfferPane(a);
        clearOfferDetail();
        showOfferActionBox(true);
        updateDeleteVisibility(a);
    }
    //Visualizzazione deggli annunci tramite offerta
    public void setAnnuncioWithOffer(annuncio a, offerta offer) {
        setAnnuncio(a);
        showOfferDetail(offer);
        showOfferActionBox(false);
    }

    // Mostra il bottone rimozione solo al venditore.
    private void updateDeleteVisibility(annuncio a) {
        if (btnDeleteAnnuncio == null) return;
        String me = Session.getMatricola();
        String venditore = a != null && a.getVenditore() != null ? a.getVenditore().getMatricola() : null;
        boolean canDelete = me != null && venditore != null && me.equalsIgnoreCase(venditore);
        btnDeleteAnnuncio.setVisible(canDelete);
        btnDeleteAnnuncio.setManaged(canDelete);
    }

    @FXML
    // Rimuove (annulla) l'annuncio se l'utente e' il venditore.
    private void handleDeleteAnnuncio() {
        if (currentAnnuncio == null) return;
        String me = Session.getMatricola();
        String venditore = currentAnnuncio.getVenditore() != null ? currentAnnuncio.getVenditore().getMatricola() : null;
        if (me == null || venditore == null || !me.equalsIgnoreCase(venditore)) {
            return;
        }

        javafx.scene.control.Alert confirm = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Rimuovi annuncio");
        confirm.setHeaderText("Sei sicuro di voler rimuovere l'annuncio?");
        confirm.setContentText("L'annuncio verra' annullato e non sara' piu' visibile.");
        var result = confirm.showAndWait();
        if (result.isEmpty() || result.get() != javafx.scene.control.ButtonType.OK) {
            return;
        }

        try (Connection conn = DB.getConnection()) {
            conn.setAutoCommit(false);
            try (var ps = conn.prepareStatement(
                    "UPDATE annuncio SET stato = 'annullato'::stato_annuncio_enum WHERE id_annuncio = ? AND matricola_venditore = ?")) {
                ps.setInt(1, currentAnnuncio.getIdAnnuncio());
                ps.setString(2, me);
                int updated = ps.executeUpdate();
                if (updated == 0) {
                    conn.rollback();
                    new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.WARNING, "Operazione non consentita.").showAndWait();
                    return;
                }
            }
            try (var ps = conn.prepareStatement(
                    "UPDATE offerta SET stato = 'rifiutata'::stato_offerta_enum WHERE id_annuncio = ? AND stato = 'in_attesa'::stato_offerta_enum")) {
                ps.setInt(1, currentAnnuncio.getIdAnnuncio());
                ps.executeUpdate();
            }
            conn.commit();
        } catch (Exception ex) {
            ex.printStackTrace();
            new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.ERROR, "Errore durante la rimozione.").showAndWait();
            return;
        }

        onDeleted.run();
        onClose.run();
    }


    // Carica le immagini dal DB e prepara la galleria.
    private void loadImages(annuncio a) {
        immagini.clear();
        if (a == null) {
            showFallback();
            toggleArrows(false);
            return;
        }

        try (Connection conn = DB.getConnection()) {
            immagineAnnuncioDAO dao = new immagineAnnuncioDAO(conn);
            immagini.addAll(dao.getImagesByAnnuncio(a.getIdAnnuncio()));
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        ensureMainFirst(a.getImmaginePath());

        //Se il DAO non ha dato nulla ma abbiamo comunque un path nel annuncio
        if (immagini.isEmpty() && a.getImmaginePath() != null && !a.getImmaginePath().isBlank()) {
            immagineAnnuncio img = new immagineAnnuncio();
            img.setIdAnnuncio(a.getIdAnnuncio());
            img.setPath(a.getImmaginePath());
            img.setPrincipale(true);
            immagini.add(img);
        }
        //Se invece è ancora vuota niente frecce
        if (immagini.isEmpty()) {
            showFallback();
            toggleArrows(false);
            return;
        }

        currentImageIndex = 0;
        showImageAt(currentImageIndex);
        toggleArrows(immagini.size() > 1);
    }
    //Si assicura che la immagine del annuncio sia nella prima posizione (0)
    private void ensureMainFirst(String mainPath) {
        if (mainPath == null || mainPath.isBlank()) return;
        int idx = -1;
        for (int i = 0; i < immagini.size(); i++) {
            immagineAnnuncio img = immagini.get(i);
            if (img != null && mainPath.equals(img.getPath())) {
                idx = i;
                break;
            }
        }
        if (idx == -1) {
            immagineAnnuncio img = new immagineAnnuncio();
            img.setPath(mainPath);
            img.setPrincipale(true);
            immagini.add(0, img);
        } else if (idx > 0) {
            immagineAnnuncio img = immagini.remove(idx);
            immagini.add(0, img);
        }
    }

    // Mostra l'immagine all'indice richiesto con wrap circolare.
    private void showImageAt(int index) {
        if (immagini.isEmpty()) {
            showFallback();
            return;
        }

        currentImageIndex = ((index % immagini.size()) + immagini.size()) % immagini.size();
        String path = immagini.get(currentImageIndex).getPath();
        imgMain.setImage(loadImage(path));
    }
    // Risolve path relativo/assoluto e carica l'immagine (fallback se manca).
    private Image loadImage(String dbPath) {
        if (dbPath == null || dbPath.isBlank()) {
            return getFallbackImage();
        }

        Path p = Path.of(dbPath);
        if (!p.isAbsolute()) {
            p = Path.of(System.getProperty("user.dir")).resolve(dbPath);
        }

        if (Files.exists(p)) {
            return new Image(p.toUri().toString(), true);
        }

        return getFallbackImage();
    }

    // Abilita/disabilita i pulsanti di navigazione immagini.
    private void toggleArrows(boolean show) {
        if (btnPrevImg != null && btnNextImg != null) {
            btnPrevImg.setVisible(show);
            btnPrevImg.setManaged(show);
            btnNextImg.setVisible(show);
            btnNextImg.setManaged(show);
        }
    }
    // Mostra l'immagine di fallback.
    private void showFallback() {
        imgMain.setImage(getFallbackImage());
    }

    // Carica il pannello offerta corretto in base al tipo annuncio.
    private void loadOfferPane(annuncio a) {
        offerContainer.getChildren().clear();

        try {
            String fxml;
            if (a.getTipo() == tipoAnnuncio.vendita) fxml = "/com/example/uninaswapoobd2425/offerVendita.fxml";
            else if (a.getTipo() == tipoAnnuncio.scambio) fxml = "/com/example/uninaswapoobd2425/offerScambio.fxml";
            else fxml = "/com/example/uninaswapoobd2425/offerRegalo.fxml";

            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxml));
            Parent pane = loader.load();

            // init controller specifico
            Object ctrl = loader.getController();
            if (ctrl instanceof offerPaneController opc) {
                opc.init(a);
            }

            offerContainer.getChildren().add(pane);

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    // Nasconde il riquadro dei dettagli offerta.
    private void clearOfferDetail() {
        if (offerDetailBox == null || offerDetailContent == null) return;
        offerDetailContent.getChildren().clear();
        offerDetailBox.setVisible(false);
        offerDetailBox.setManaged(false);
    }

    //Mostra i dettagli in base al tipo dell offerta (QUESTO E' PER OFFERTE INVIATE/RICEVUTE)
    private void showOfferDetail(offerta offer) {
        if (offerDetailBox == null || offerDetailContent == null) return;
        offerDetailContent.getChildren().clear();
        if (offer == null) {
            offerDetailBox.setVisible(false);
            offerDetailBox.setManaged(false);
            return;
        }

        annuncio a = offer.getAnnuncio();
        tipoAnnuncio tipo = a != null ? a.getTipo() : null;
        if (tipo == tipoAnnuncio.vendita) {
            addOfferRow("Offerta inviata con prezzo richiesto:", formatEuro(a != null && a.getPrezzo() != null ? a.getPrezzo() : currentAnnuncio.getPrezzo()));
            Region spacer = new Region();
            spacer.setPrefHeight(6);
            offerDetailContent.getChildren().add(spacer);
            addOfferRow("Prezzo offerto:", formatEuro(offer.getImportoProposto()));
            addOfferRow("Nota per il venditore:", formatText(offer.getMessaggio()));
        } else if (tipo == tipoAnnuncio.scambio) {
            addOfferRow("Offerta:", "");
            List<oggettoScambio> items = loadScambioItems(offer.getIdOfferta());
            if (items.isEmpty()) {
                addOfferRow("", "(Nessun oggetto)");
            } else {
                VBox list = new VBox(6);
                for (oggettoScambio it : items) {
                    HBox row = new HBox(8);
                    ImageView iv = new ImageView();
                    iv.setFitWidth(40);
                    iv.setFitHeight(40);
                    iv.setPreserveRatio(true);
                    Image img = loadImageFromPath(it.getPath());
                    if (img != null) iv.setImage(img);

                    String nome = it.getNomeOggetto();
                    Label name = new Label(nome != null && !nome.isBlank() ? nome.trim() : "Oggetto");
                    name.getStyleClass().add("offer-detail-value");
                    row.getChildren().addAll(iv, name);
                    list.getChildren().add(row);
                }
                offerDetailContent.getChildren().add(list);
            }
            addOfferRow("Messaggio:", formatText(offer.getMessaggio()));
        } else if (tipo == tipoAnnuncio.regalo) {
            addOfferRow("Messaggio:", formatText(offer.getMessaggio()));
        }

        offerDetailBox.setVisible(true);
        offerDetailBox.setManaged(true);
    }

    // Carica gli oggetti di scambio per l'offerta.
    private List<oggettoScambio> loadScambioItems(int idOfferta) {
        List<oggettoScambio> out = new ArrayList<>();
        try (Connection conn = DB.getConnection()) {
            oggettoScambioDAO dao = new oggettoScambioDAO(conn);
            out.addAll(dao.getByOfferta(idOfferta));
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return out;
    }

    // Carica un'immagine da path (gestendo path relativi).
    private Image loadImageFromPath(String path) {
        if (path == null || path.isBlank()) return null;
        Path p = Path.of(path);
        if (!p.isAbsolute()) {
            p = Path.of(System.getProperty("user.dir")).resolve(path);
        }
        if (Files.exists(p)) {
            return new Image(p.toUri().toString(), true);
        }
        return null;
    }

    // Aggiunge una riga etichetta/valore ai dettagli offerta.
    private void addOfferRow(String label, String value) {
        HBox row = new HBox(6);
        row.setFillHeight(true);
        Label l = new Label(label);
        l.getStyleClass().add("offer-detail-label");
        Label v = new Label(value);
        v.getStyleClass().add("offer-detail-value");
        v.setWrapText(true);
        row.getChildren().addAll(l, v);
        offerDetailContent.getChildren().add(row);
    }

    // Formatta importi in euro per la UI.
    private String formatEuro(BigDecimal v) {
        if (v == null) return "-";
        return "€ " + v;
    }

    // Normalizza testo per UI con fallback.
    private String formatText(String s) {
        if (s == null || s.isBlank()) return "(Nessun messaggio)";
        return s.trim();
    }

    // Mostra/nasconde il box azioni offerta.
    private void showOfferActionBox(boolean show) {
        if (offerActionBox == null) return;
        offerActionBox.setVisible(show);
        offerActionBox.setManaged(show);
    }

    // Applica angoli arrotondati all'immagine principale.
    private void roundImage(ImageView iv, double arc) {
        Rectangle clip = new Rectangle();
        clip.setArcWidth(arc);
        clip.setArcHeight(arc);
        clip.widthProperty().bind(iv.fitWidthProperty());
        clip.heightProperty().bind(iv.fitHeightProperty());
        iv.setClip(clip);
    }

    // Limita il rendering dell'immagine ai bordi della card.
    private void clipToCardBounds() {
        Rectangle clip = new Rectangle();
        clip.widthProperty().bind(detailCard.widthProperty());
        clip.heightProperty().bind(detailCard.heightProperty());
        detailCard.setClip(clip);
    }

    @FXML
    // Mostra immagine precedente.
    private void handlePrevImage() {
        if (!immagini.isEmpty()) {
            showImageAt(currentImageIndex - 1);
        }
    }

    @FXML
    // Mostra immagine successiva.
    private void handleNextImage() {
        if (!immagini.isEmpty()) {
            showImageAt(currentImageIndex + 1);
        }
    }

    @FXML
    // Apre una finestra con anteprima immagine ingrandita.
    private void handleImageClick() {
        Image img = imgMain.getImage();
        if (img == null) return;

        ImageView iv = new ImageView(img);
        iv.setPreserveRatio(true);
        iv.setFitWidth(900);
        iv.setFitHeight(700);

        StackPane pane = new StackPane(iv);
        pane.setStyle("-fx-background-color: rgba(0,0,0,0.85);");

        Stage stage = new Stage();
        stage.setTitle("Anteprima immagine");
        stage.setScene(new Scene(pane));
        stage.show();

        pane.setOnMouseClicked(e -> stage.close());
    }

    @FXML
    // Mostra la descrizione completa in una finestra custom (stile recensione).
    private void handleShowDescription() {
        String text = lblDescrizione != null ? lblDescrizione.getText() : null;
        if (text == null || text.isBlank()) return;

        Stage owner = (Stage) dialogRoot.getScene().getWindow();
        Stage stage = new Stage(StageStyle.UNDECORATED);
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.initOwner(owner);

        BorderPane root = new BorderPane();
        root.setPadding(new Insets(14));
        root.setStyle("-fx-background-color: #F7F2EA; -fx-background-radius: 18; -fx-border-radius: 18; -fx-border-color: rgba(0,0,0,0.10); -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.18), 18, 0.22, 0, 6);");

        Label title = new Label("Descrizione completa");
        title.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #2B2B2B;");

        TextArea area = new TextArea(text);
        area.setWrapText(true);
        area.setEditable(false);
        area.setPrefRowCount(10);
        area.setPrefColumnCount(42);
        area.setStyle("-fx-background-color: transparent; -fx-control-inner-background: #FFF9F2; -fx-background-radius: 12; -fx-border-radius: 12; -fx-border-color: rgba(0,0,0,0.08);");

        Button btnClose = new Button("Chiudi");
        btnClose.getStyleClass().add("btn-ghost");
        btnClose.setOnAction(e -> stage.close());
        btnClose.setPrefWidth(140);
        btnClose.setMinWidth(140);

        HBox buttons = new HBox(12, new Region(), btnClose);
        HBox.setHgrow(buttons.getChildren().get(0), Priority.ALWAYS);

        VBox content = new VBox(12, title, area, buttons);
        root.setCenter(content);

        Scene scene = new Scene(root, 520, 360);
        String css = Optional.ofNullable(getClass().getResource("/com/example/uninaswapoobd2425/style.css"))
                .map(URL::toExternalForm)
                .orElse(null);
        if (css != null) scene.getStylesheets().add(css);
        stage.setScene(scene);

        if (owner != null) {
            stage.setX(owner.getX() + (owner.getWidth() - scene.getWidth()) / 2);
            stage.setY(owner.getY() + (owner.getHeight() - scene.getHeight()) / 2);
        }

        stage.showAndWait();
    }

    // Restituisce l'immagine di fallback caricata una sola volta.
    private Image getFallbackImage() {
        if (fallbackImage == null) {
            fallbackImage = new Image(Objects.requireNonNull(
                    getClass().getResourceAsStream("/com/example/uninaswapoobd2425/imgs/prov.png")
            ));
        }
        return fallbackImage;
    }

    // Capitalizza la prima lettera e sostituisce underscore.
    private String capFirst(String s) {
        if (s == null || s.isEmpty()) return "";
        return s.substring(0,1).toUpperCase() + s.substring(1).replace("_", " ");
    }

    // Restituisce la label UI per il tipo annuncio.
    private String labelTipo(tipoAnnuncio t) {
        return switch (t) {
            case vendita -> "In vendita";
            case scambio -> "Scambio";
            case regalo -> "Regalo";
        };
    }
}
