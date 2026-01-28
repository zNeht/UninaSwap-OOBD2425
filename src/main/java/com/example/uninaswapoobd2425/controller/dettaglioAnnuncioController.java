package com.example.uninaswapoobd2425.controller;

import com.example.uninaswapoobd2425.dao.DB;
import com.example.uninaswapoobd2425.dao.immagineAnnuncioDAO;
import com.example.uninaswapoobd2425.model.annuncio;
import com.example.uninaswapoobd2425.model.tipoAnnuncio;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;

import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

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

    @FXML private VBox offerContainer;

    private final List<String> immagini = new ArrayList<>();
    private int currentImageIndex = 0;
    private annuncio currentAnnuncio;
    private Image fallbackImage;
    private Runnable onClose = () -> {};

    @FXML
    private void initialize() {
        imgMain.fitWidthProperty().bind(dialogRoot.widthProperty().subtract(28));
        clipToCardBounds();
        roundImage(imgMain, 18);
        toggleArrows(false);
    }

    public void setOnClose(Runnable r) { this.onClose = r; }

    @FXML
    private void handleClose() { onClose.run(); }

    public void setAnnuncio(annuncio a) {
        this.currentAnnuncio = a;
        lblTitolo.setText(a.getTitolo());
        lblDescrizione.setText(a.getDescrizione());

        // categoria con prima lettera maiuscola
        lblCategoria.setText(capFirst(a.getCategoria().name()));

        // badge tipo
        lblTipoBadge.setText(labelTipo(a.getTipo()));

        lblVenditoreEmail.setText(
                a.getVenditoreEmail() != null ? a.getVenditoreEmail() : "-"
        );

        // prezzo
        if (a.getTipo() == tipoAnnuncio.vendita) {
            lblPrezzo.setText("€ " + a.getPrezzo().toString());
        } else if (a.getTipo() == tipoAnnuncio.regalo) {
            lblPrezzo.setText("Gratis");
        } else {
            lblPrezzo.setText("Scambio");
        }

        loadImages(a);
        loadOfferPane(a);
    }

    private void loadImages(annuncio a) {
        immagini.clear();
        if (a == null) {
            showFallback();
            toggleArrows(false);
            return;
        }

        try (Connection conn = DB.getConnection()) {
            immagineAnnuncioDAO dao = new immagineAnnuncioDAO(conn);
            immagini.addAll(dao.getImagePathsByAnnuncio(a.getIdAnnuncio()));
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        ensureMainFirst(a.getImmaginePath());

        if (immagini.isEmpty() && a.getImmaginePath() != null && !a.getImmaginePath().isBlank()) {
            immagini.add(a.getImmaginePath());
        }

        if (immagini.isEmpty()) {
            showFallback();
            toggleArrows(false);
            return;
        }

        currentImageIndex = 0;
        showImageAt(currentImageIndex);
        toggleArrows(immagini.size() > 1);
    }

    private void ensureMainFirst(String mainPath) {
        if (mainPath == null || mainPath.isBlank()) return;
        int idx = immagini.indexOf(mainPath);
        if (idx == -1) {
            immagini.add(0, mainPath);
        } else if (idx > 0) {
            immagini.remove(idx);
            immagini.add(0, mainPath);
        }
    }

    private void showImageAt(int index) {
        if (immagini.isEmpty()) {
            showFallback();
            return;
        }

        currentImageIndex = ((index % immagini.size()) + immagini.size()) % immagini.size();
        String path = immagini.get(currentImageIndex);
        imgMain.setImage(loadImage(path));
    }

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

    private void toggleArrows(boolean show) {
        if (btnPrevImg != null && btnNextImg != null) {
            btnPrevImg.setVisible(show);
            btnPrevImg.setManaged(show);
            btnNextImg.setVisible(show);
            btnNextImg.setManaged(show);
        }
    }

    private void showFallback() {
        imgMain.setImage(getFallbackImage());
    }

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

    private void roundImage(ImageView iv, double arc) {
        Rectangle clip = new Rectangle();
        clip.setArcWidth(arc);
        clip.setArcHeight(arc);
        clip.widthProperty().bind(iv.fitWidthProperty());
        clip.heightProperty().bind(iv.fitHeightProperty());
        iv.setClip(clip);
    }

    private void clipToCardBounds() {
        Rectangle clip = new Rectangle();
        clip.widthProperty().bind(detailCard.widthProperty());
        clip.heightProperty().bind(detailCard.heightProperty());
        detailCard.setClip(clip);
    }

    @FXML
    private void handlePrevImage() {
        if (!immagini.isEmpty()) {
            showImageAt(currentImageIndex - 1);
        }
    }

    @FXML
    private void handleNextImage() {
        if (!immagini.isEmpty()) {
            showImageAt(currentImageIndex + 1);
        }
    }

    @FXML
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

    private Image getFallbackImage() {
        if (fallbackImage == null) {
            fallbackImage = new Image(Objects.requireNonNull(
                    getClass().getResourceAsStream("/com/example/uninaswapoobd2425/imgs/prov.png")
            ));
        }
        return fallbackImage;
    }

    private String capFirst(String s) {
        if (s == null || s.isEmpty()) return "";
        return s.substring(0,1).toUpperCase() + s.substring(1).replace("_", " ");
    }

    private String labelTipo(tipoAnnuncio t) {
        return switch (t) {
            case vendita -> "In vendita";
            case scambio -> "Scambio";
            case regalo -> "Regalo";
        };
    }
}
