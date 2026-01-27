package com.example.uninaswapoobd2425.controller;

import com.example.uninaswapoobd2425.model.annuncio;
import com.example.uninaswapoobd2425.model.tipoAnnuncio;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Rectangle;

import java.util.Objects;

public class dettaglioAnnuncioController {
    @FXML private StackPane dialogRoot;
    @FXML private VBox detailCard;
    @FXML private ImageView imgMain;
    @FXML private Label lblTipoBadge;
    @FXML private Label lblTitolo;
    @FXML private Label lblPrezzo;
    @FXML private Label lblCategoria;
    @FXML private Label lblDescrizione;
    @FXML private Label lblLuogo;
    @FXML private Label lblVenditore;
    @FXML private Label lblVenditoreEmail;

    @FXML private VBox offerContainer;

    private Runnable onClose = () -> {};
    @FXML
    private void initialize() {
        imgMain.fitWidthProperty().bind(dialogRoot.widthProperty().subtract(28));
        clipToCardBounds();
    }

    public void setOnClose(Runnable r) { this.onClose = r; }

    @FXML
    private void handleClose() { onClose.run(); }

    public void setAnnuncio(annuncio a) {
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

        // immagine (se hai path dal DB)
        if (a.getImmaginePath() != null) {
            imgMain.setImage(new Image("file:" + a.getImmaginePath(), true));
        } else {
            // fallback
            imgMain.setImage(new Image(Objects.requireNonNull(
                    getClass().getResourceAsStream("/com/example/uninaswapoobd2425/imgs/prov.png")
            )));
        }

        roundImage(imgMain, 18);



        loadOfferPane(a);
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
