package com.example.uninaswapoobd2425.controller;

import com.example.uninaswapoobd2425.model.annuncio;
import com.example.uninaswapoobd2425.model.offerta;
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
import javafx.scene.control.ButtonBase;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.input.MouseEvent;
import javafx.scene.Node;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.scene.text.Text;

import java.io.File;
import java.net.URL;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.function.Consumer;

public class offertaCardController {
    @FXML private VBox root;
    @FXML private Label lblStato;
    @FXML private Label lblTitolo;
    @FXML private Label lblImporto;
    @FXML private Label lblPrezzoRichiesto;
    @FXML private Label lblVenditore;
    @FXML private Label lblData;
    @FXML private ImageView imgAnnuncio;
    @FXML private HBox actionBar;
    @FXML private Button btnOggetti;
    @FXML private Button btnMessaggio;
    @FXML private Button btnEditMessaggio;
    @FXML private Button btnModifica;
    @FXML private Button btnRitira;
    @FXML private Button btnRecensisci;

    private Runnable onModifica = () -> {};
    private Runnable onRitira = () -> {};
    private Runnable onViewItems = () -> {};
    private Runnable onViewMessage = () -> {};
    private Runnable onEditMessage = () -> {};
    private Consumer<RecensioneInput> onRecensisci = r -> {};
    private Runnable onOpenDetails = () -> {};

    @FXML
    // Inizializza visibilita' pulsanti e click sulla card.
    private void initialize() {
        showReviewButton(false);
        showModifyWithdraw(true);
        showItemsButton(false);
        showMessageButton(false);
        showEditMessageButton(false);
        if (root != null) {
            root.addEventFilter(MouseEvent.MOUSE_CLICKED, this::handleCardClick);
        }
    }

    // Popola la card con i dati dell'offerta.
    public void setData(offerta v) {
        annuncio a = v != null ? v.getAnnuncio() : null;
        lblTitolo.setText(a != null && a.getTitolo() != null ? a.getTitolo() : "-");
        lblStato.setText(labelStato(v.getStato()));

        if (v.getImportoProposto() != null) {
            lblImporto.setText("€ " + v.getImportoProposto());
        } else {
            tipoAnnuncio tipo = a != null ? a.getTipo() : null;
            lblImporto.setText(tipo == tipoAnnuncio.scambio ? "Scambio" : "Gratis");
        }

        if (a != null && a.getPrezzo() != null) {
            lblPrezzoRichiesto.setText("€ " + a.getPrezzo());
            if (v.getImportoProposto() != null && v.getImportoProposto().compareTo(a.getPrezzo()) < 0) {
                lblPrezzoRichiesto.getStyleClass().add("strike");
            } else {
                lblPrezzoRichiesto.getStyleClass().remove("strike");
            }
        } else {
            lblPrezzoRichiesto.setText("-");
        }

        if (lblVenditore != null) {
            String venditore = a != null && a.getVenditore() != null ? a.getVenditore().getMatricola() : null;
            lblVenditore.setText(venditore != null && !venditore.isBlank()
                    ? venditore
                    : "-");
        }

        if (v.getDataOfferta() != null) {
            lblData.setText(v.getDataOfferta().toLocalDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
        } else {
            lblData.setText("-");
        }

        if (a != null && a.getImmaginePath() != null && !a.getImmaginePath().isBlank()) {
            File f = new File(System.getProperty("user.dir"), a.getImmaginePath());
            if (f.exists()) {
                imgAnnuncio.setImage(new Image(f.toURI().toString(), true));
            }
        } else {
            imgAnnuncio.setImage(new Image(
                    getClass().getResource("/com/example/uninaswapoobd2425/imgs/prov.png").toExternalForm()
            ));
        }

        setStatusStyle(v.getStato());
    }

    // Mostra o nasconde la barra azioni.
    public void setActionsVisible(boolean visible) {
        actionBar.setManaged(visible);
        actionBar.setVisible(visible);
    }

    // Cambia testo del bottone modifica.
    public void setModificaLabel(String text) {
        if (text != null && !text.isBlank()) {
            btnModifica.setText(text);
        }
    }

    // Cambia testo del bottone ritira.
    public void setRitiraLabel(String text) {
        if (text != null && !text.isBlank()) {
            btnRitira.setText(text);
        }
    }

    // Abilita/disabilita i pulsanti in base ai permessi.
    public void setActionStates(boolean canModify, boolean canWithdraw) {
        btnModifica.setDisable(!canModify);
        btnRitira.setDisable(!canWithdraw);
    }

    // Mostra/nasconde il bottone recensione.
    public void showReviewButton(boolean show) {
        btnRecensisci.setVisible(show);
        btnRecensisci.setManaged(show);
    }

    // Mostra/nasconde i bottoni modifica/ritira.
    public void showModifyWithdraw(boolean show) {
        btnModifica.setVisible(show);
        btnModifica.setManaged(show);
        btnRitira.setVisible(show);
        btnRitira.setManaged(show);
    }

    // Mostra/nasconde il bottone oggetti.
    public void showItemsButton(boolean show) {
        if (btnOggetti != null) {
            btnOggetti.setVisible(show);
            btnOggetti.setManaged(show);
        }
    }

    // Mostra/nasconde il bottone messaggio.
    public void showMessageButton(boolean show) {
        if (btnMessaggio != null) {
            btnMessaggio.setVisible(show);
            btnMessaggio.setManaged(show);
        }
    }

    // Mostra/nasconde il bottone modifica messaggio.
    public void showEditMessageButton(boolean show) {
        if (btnEditMessaggio != null) {
            btnEditMessaggio.setVisible(show);
            btnEditMessaggio.setManaged(show);
        }
    }

    // Aggiorna il testo del bottone messaggio (aggiungi/modifica).
    public void setEditMessageLabel(String text) {
        if (btnEditMessaggio != null && text != null && !text.isBlank()) {
            btnEditMessaggio.setText(text);
        }
    }

    // Imposta handler per modifica e ritira.
    public void setHandlers(Runnable onModifica, Runnable onRitira) {
        this.onModifica = onModifica != null ? onModifica : () -> {};
        this.onRitira = onRitira != null ? onRitira : () -> {};
    }

    // Imposta handler per invio recensione.
    public void setOnRecensisci(Consumer<RecensioneInput> handler) {
        this.onRecensisci = handler != null ? handler : r -> {};
    }

    // Imposta handler per visualizzare oggetti scambio.
    public void setOnViewItems(Runnable handler) {
        this.onViewItems = handler != null ? handler : () -> {};
    }

    // Imposta handler per visualizzare messaggio.
    public void setOnViewMessage(Runnable handler) {
        this.onViewMessage = handler != null ? handler : () -> {};
    }

    // Imposta handler per aggiungere/modificare messaggio.
    public void setOnEditMessage(Runnable handler) {
        this.onEditMessage = handler != null ? handler : () -> {};
    }

    // Imposta handler per apertura dettaglio.
    public void setOnOpenDetails(Runnable handler) {
        this.onOpenDetails = handler != null ? handler : () -> {};
    }

    // Gestisce click sulla card evitando i controlli interni.
    private void handleCardClick(MouseEvent event) {
        if (event == null) return;
        Node target = event.getTarget() instanceof Node ? (Node) event.getTarget() : null;
        if (target == null) return;
        if (isInsideButton(target)) return;
        if (isInsideLabelOrText(target)) return;
        onOpenDetails.run();
    }

    // Verifica se il click e' avvenuto dentro un bottone.
    private boolean isInsideButton(Node node) {
        while (node != null) {
            if (node instanceof ButtonBase) return true;
            node = node.getParent();
        }
        return false;
    }

    // Verifica se il click e' avvenuto su testo/label.
    private boolean isInsideLabelOrText(Node node) {
        while (node != null) {
            if (node instanceof Label || node instanceof Text) return true;
            node = node.getParent();
        }
        return false;
    }

    @FXML
    // Handler UI per modifica.
    private void handleModifica() {
        onModifica.run();
    }

    @FXML
    // Handler UI per ritira.
    private void handleRitira() {
        onRitira.run();
    }

    @FXML
    // Handler UI per mostrare oggetti.
    private void handleViewItems() {
        onViewItems.run();
    }

    @FXML
    // Handler UI per mostrare messaggio.
    private void handleViewMessage() {
        onViewMessage.run();
    }

    @FXML
    // Handler UI per aggiungere/modificare messaggio.
    private void handleEditMessage() {
        onEditMessage.run();
    }

    @FXML
    // Handler UI per apertura dialog recensione.
    private void handleRecensisci() {
        RecensioneInput input = openRecensioneDialog();
        if (input == null) return;
        onRecensisci.accept(input);
    }

    // Dialog per inserimento recensione (stelle + commento).
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

    // Aggiorna la UI delle stelle del rating.
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

    // Converte lo stato in label leggibile.
    private String labelStato(statoOfferta stato) {
        return switch (stato) {
            case in_attesa -> "In attesa";
            case accettata -> "Accettata";
            case rifiutata -> "Rifiutata";
            case ritirata -> "Ritirata";
        };
    }

    // Applica stile CSS in base allo stato.
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
