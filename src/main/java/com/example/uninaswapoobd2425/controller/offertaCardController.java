package com.example.uninaswapoobd2425.controller;

import com.example.uninaswapoobd2425.dao.offertaDAO;
import com.example.uninaswapoobd2425.model.statoOfferta;
import com.example.uninaswapoobd2425.model.tipoAnnuncio;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.File;
import java.net.URL;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.function.Consumer;

public class offertaCardController {
    @FXML private Label lblStato;
    @FXML private Label lblTitolo;
    @FXML private Label lblImporto;
    @FXML private Label lblPrezzoRichiesto;
    @FXML private Label lblVenditore;
    @FXML private Label lblData;
    @FXML private ImageView imgAnnuncio;
    @FXML private HBox actionBar;
    @FXML private Button btnOggetti;
    @FXML private Button btnModifica;
    @FXML private Button btnRitira;
    @FXML private Button btnRecensisci;

    private Runnable onModifica = () -> {};
    private Runnable onRitira = () -> {};
    private Runnable onViewItems = () -> {};
    private Consumer<RecensioneInput> onRecensisci = r -> {};

    @FXML
    private void initialize() {
        showReviewButton(false);
        showModifyWithdraw(true);
        showItemsButton(false);
    }

    public void setData(offertaDAO.OfferView v) {
        lblTitolo.setText(v.titoloAnnuncio);
        lblStato.setText(labelStato(v.stato));

        if (v.importo != null) {
            lblImporto.setText("€ " + v.importo);
        } else {
            lblImporto.setText(v.tipo == tipoAnnuncio.scambio ? "Scambio" : "Gratis");
        }

        if (v.prezzoRichiesto != null) {
            lblPrezzoRichiesto.setText("€ " + v.prezzoRichiesto);
            if (v.importo != null && v.importo.compareTo(v.prezzoRichiesto) < 0) {
                lblPrezzoRichiesto.getStyleClass().add("strike");
            } else {
                lblPrezzoRichiesto.getStyleClass().remove("strike");
            }
        } else {
            lblPrezzoRichiesto.setText("-");
        }

        if (lblVenditore != null) {
            lblVenditore.setText(v.matricolaVenditore != null && !v.matricolaVenditore.isBlank()
                    ? v.matricolaVenditore
                    : "-");
        }

        if (v.dataOfferta != null) {
            lblData.setText(v.dataOfferta.toLocalDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
        } else {
            lblData.setText("-");
        }

        if (v.imgPath != null && !v.imgPath.isBlank()) {
            File f = new File(System.getProperty("user.dir"), v.imgPath);
            if (f.exists()) {
                imgAnnuncio.setImage(new Image(f.toURI().toString(), true));
            }
        } else {
            imgAnnuncio.setImage(new Image(
                    getClass().getResource("/com/example/uninaswapoobd2425/imgs/prov.png").toExternalForm()
            ));
        }

        setStatusStyle(v.stato);
    }

    public void setActionsVisible(boolean visible) {
        actionBar.setManaged(visible);
        actionBar.setVisible(visible);
    }

    public void setModificaLabel(String text) {
        if (text != null && !text.isBlank()) {
            btnModifica.setText(text);
        }
    }

    public void setRitiraLabel(String text) {
        if (text != null && !text.isBlank()) {
            btnRitira.setText(text);
        }
    }

    public void setActionStates(boolean canModify, boolean canWithdraw) {
        btnModifica.setDisable(!canModify);
        btnRitira.setDisable(!canWithdraw);
    }

    public void showReviewButton(boolean show) {
        btnRecensisci.setVisible(show);
        btnRecensisci.setManaged(show);
    }

    public void showModifyWithdraw(boolean show) {
        btnModifica.setVisible(show);
        btnModifica.setManaged(show);
        btnRitira.setVisible(show);
        btnRitira.setManaged(show);
    }

    public void showItemsButton(boolean show) {
        if (btnOggetti != null) {
            btnOggetti.setVisible(show);
            btnOggetti.setManaged(show);
        }
    }

    public void setHandlers(Runnable onModifica, Runnable onRitira) {
        this.onModifica = onModifica != null ? onModifica : () -> {};
        this.onRitira = onRitira != null ? onRitira : () -> {};
    }

    public void setOnRecensisci(Consumer<RecensioneInput> handler) {
        this.onRecensisci = handler != null ? handler : r -> {};
    }

    public void setOnViewItems(Runnable handler) {
        this.onViewItems = handler != null ? handler : () -> {};
    }

    @FXML
    private void handleModifica() {
        onModifica.run();
    }

    @FXML
    private void handleRitira() {
        onRitira.run();
    }

    @FXML
    private void handleViewItems() {
        onViewItems.run();
    }

    @FXML
    private void handleRecensisci() {
        RecensioneInput input = openRecensioneDialog();
        if (input == null) return;
        onRecensisci.accept(input);
    }

    private RecensioneInput openRecensioneDialog() {
        Stage owner = (Stage) btnRecensisci.getScene().getWindow();
        Stage stage = new Stage(StageStyle.UNDECORATED);
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.initOwner(owner);

        BorderPane root = new BorderPane();
        root.setPadding(new Insets(14));
        root.setStyle("-fx-background-color: #F7F2EA; -fx-background-radius: 18; -fx-border-radius: 18; -fx-border-color: rgba(0,0,0,0.10); -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.18), 18, 0.22, 0, 6);");

        // Submit button declared early to enable after rating click
        Button btnSubmit = new Button("Invia recensione");
        btnSubmit.getStyleClass().add("btn-primary");
        btnSubmit.setDisable(true);

        // Content
        HBox stars = new HBox(8);
        stars.setAlignment(Pos.CENTER_LEFT);
        Button[] starBtns = new Button[5];
        final int[] rating = {0};
        for (int i = 0; i < 5; i++) {
            int idx = i + 1;
            Button b = new Button("☆");
            b.setStyle("-fx-background-color: transparent; -fx-font-size: 20px;");
            b.setOnAction(e -> {
                rating[0] = idx;
                updateStars(starBtns, idx);
                btnSubmit.setDisable(rating[0] == 0);
            });
            starBtns[i] = b;
            stars.getChildren().add(b);
        }

        TextArea comment = new TextArea();
        comment.setPromptText("Commento (max 100 caratteri)");
        comment.setWrapText(true);
        comment.setPrefRowCount(3);
        comment.textProperty().addListener((obs, o, n) -> {
            if (n != null && n.length() > 100) {
                comment.setText(n.substring(0, 100));
            }
        });

        VBox content = new VBox(12);
        content.setPadding(new Insets(4, 0, 4, 0));
        content.getChildren().addAll(
                new Label("Valutazione:"), stars,
                new Label("Commento:"), comment
        );

        // Buttons
        Button btnCancel = new Button("Annulla");
        btnCancel.getStyleClass().add("btn-ghost");
        btnCancel.setOnAction(e -> stage.close());

        btnSubmit.setOnAction(e -> {
            if (rating[0] == 0) return;
            RecensioneInput r = new RecensioneInput();
            r.stelle = rating[0];
            r.commento = comment.getText() == null ? "" : comment.getText().trim();
            stage.setUserData(r);
            stage.close();
        });
        btnSubmit.setPrefWidth(200);
        btnSubmit.setMaxWidth(Region.USE_PREF_SIZE);
        btnSubmit.setMinWidth(200);

        HBox buttons = new HBox(12, btnCancel, btnSubmit);
        buttons.setAlignment(Pos.CENTER);
        buttons.setPadding(new Insets(10, 0, 0, 0));

        VBox center = new VBox(12, content, buttons);
        center.setAlignment(Pos.CENTER_LEFT);

        root.setCenter(center);

        Scene scene = new Scene(root, 420, 260);
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

        Object data = stage.getUserData();
        if (data instanceof RecensioneInput ri) {
            return ri;
        }
        return null;
    }

    private void updateStars(Button[] stars, int filled) {
        for (int i = 0; i < stars.length; i++) {
            stars[i].setText(i < filled ? "★" : "☆");
            stars[i].setStyle("-fx-background-color: transparent; -fx-font-size: 20px; -fx-text-fill: " + (i < filled ? "#E8CD9A" : "#777") + ";");
        }
    }

    public static class RecensioneInput {
        int stelle;
        String commento;
    }

    private String labelStato(statoOfferta stato) {
        return switch (stato) {
            case in_attesa -> "In attesa";
            case accettata -> "Accettata";
            case rifiutata -> "Rifiutata";
            case ritirata -> "Ritirata";
        };
    }

    private void setStatusStyle(statoOfferta stato) {
        lblStato.getStyleClass().removeAll("attesa", "accettata", "rifiutata", "ritirata");
        switch (stato) {
            case in_attesa -> lblStato.getStyleClass().add("attesa");
            case accettata -> lblStato.getStyleClass().add("accettata");
            case rifiutata -> lblStato.getStyleClass().add("rifiutata");
            case ritirata -> lblStato.getStyleClass().add("ritirata");
        }
    }
}
