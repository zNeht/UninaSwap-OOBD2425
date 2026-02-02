package com.example.uninaswapoobd2425.controller;

import com.example.uninaswapoobd2425.dao.DB;
import javafx.animation.FadeTransition;
import javafx.animation.ParallelTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.TranslateTransition;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import org.mindrot.jbcrypt.BCrypt;

public class registerController {
    private double xOffset = 0;
    private double yOffset = 0;
    @FXML
    private TextField nameField;
    @FXML
    private TextField surnameField;
    @FXML
    private TextField emailField;
    @FXML
    private TextField usernameField;
    @FXML
    private PasswordField passwordField;
    @FXML
    private PasswordField confirmPasswordField;
    @FXML
    private Pane rootPane;

    @FXML
    private Label nameLabel;
    @FXML
    private Label surnameLabel;
    @FXML
    private Label emailLabel;
    @FXML
    private Label usernameLabel;
    @FXML
    private Label passwordLabel;
    @FXML
    private Label confirmPasswordLabel;


    @FXML
    // Inizializza floating label e drag finestra.
    public void initialize() {
        setupFloatingLabel(nameField, nameLabel);
        setupFloatingLabel(surnameField, surnameLabel);
        setupFloatingLabel(emailField, emailLabel);
        setupFloatingLabel(usernameField, usernameLabel);
        setupFloatingLabel(passwordField, passwordLabel);
        setupFloatingLabel(confirmPasswordField, confirmPasswordLabel);

        if (rootPane != null) {
            rootPane.setOnMousePressed(event -> {
                xOffset = event.getSceneX();
                yOffset = event.getSceneY();
            });

            rootPane.setOnMouseDragged(event -> {
                Stage stage = (Stage) rootPane.getScene().getWindow();
                stage.setX(event.getScreenX() - xOffset);
                stage.setY(event.getScreenY() - yOffset);
            });
        }
    }

    // Gestisce l'animazione della label quando il campo e' focusato o compilato.
    private void setupFloatingLabel(TextField field, Label label) {

        TranslateTransition moveUp = new TranslateTransition(Duration.millis(200), label);
        moveUp.setToY(-25);


        TranslateTransition moveDown = new TranslateTransition(Duration.millis(200), label);
        moveDown.setToY(0);


        field.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal) {

                moveUp.play();
                label.setStyle("-fx-text-fill: #F0A500; -fx-font-size: 14px;");
            } else {

                if (field.getText().isEmpty()) {
                    moveDown.play();
                    label.setStyle("-fx-text-fill: white; -fx-font-size: 14px;");
                }
            }
        });

        field.textProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal.isEmpty() && !(label.getTranslateY() == -25)) {
                moveUp.play();
                label.setStyle("-fx-text-fill: #F0A500; -fx-font-size: 12px;");
            }
        });
    }

    @FXML
    // Chiude l'applicazione.
    void handleClose(ActionEvent event) {
        javafx.application.Platform.exit();
    }

    @FXML
    // Minimizza la finestra con animazione.
    void handleMinimize(ActionEvent event) {
        Node source = (Node) event.getSource();
        Stage stage = (Stage) source.getScene().getWindow();

        Node root = source.getScene().getRoot();

        FadeTransition fade = new FadeTransition(Duration.millis(250), root);
        fade.setFromValue(1.0);
        fade.setToValue(0.0);

        ScaleTransition scale = new ScaleTransition(Duration.millis(250), root);
        scale.setFromX(1.0);
        scale.setToX(0.8);
        scale.setFromY(1.0);
        scale.setToY(0.8);


        ParallelTransition animation = new ParallelTransition(fade, scale);

        animation.setOnFinished(e -> {
            stage.setIconified(true);

            root.setOpacity(1.0);
            root.setScaleX(1.0);
            root.setScaleY(1.0);
        });

        animation.play();
    }

    @FXML
    // Ritorna alla schermata di login con transizione.
    protected void onLoginLinkClick(ActionEvent event) {

        Node sourceNode = (Node) event.getSource();
        Scene currentScene = sourceNode.getScene();
        Parent root = currentScene.getRoot();


        FadeTransition fadeOut = new FadeTransition(Duration.millis(300), root);
        fadeOut.setFromValue(1.0);
        fadeOut.setToValue(0.0);

        fadeOut.setOnFinished(e -> {
            try {

                FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/uninaswapoobd2425/login.fxml"));
                Parent newRoot = loader.load();

                newRoot.setOpacity(0);


                Stage stage = (Stage) currentScene.getWindow();
                Scene newScene = new Scene(newRoot);
                newScene.setFill(Color.TRANSPARENT);
                stage.setScene(newScene);


                FadeTransition fadeIn = new FadeTransition(Duration.millis(300), newRoot);
                fadeIn.setFromValue(0.0);
                fadeIn.setToValue(1.0);
                fadeIn.play();

            } catch (IOException ex) {
                ex.printStackTrace();
            }
        });


        fadeOut.play();
    }

    @FXML
    // Valida input e registra un nuovo utente sul DB.
    void handleRegister(ActionEvent event) {
        String nome = safeText(nameField);
        String cognome = safeText(surnameField);
        String mail = safeText(emailField);
        String matricola = safeText(usernameField);
        String password = safeText(passwordField);
        String confirm = safeText(confirmPasswordField);

        if (nome.isEmpty() || cognome.isEmpty() || mail.isEmpty() || matricola.isEmpty() || password.isEmpty()) {
            new Alert(Alert.AlertType.WARNING, "Compila tutti i campi obbligatori.").showAndWait();
            return;
        }
        if (matricola.length() != 9) {
            new Alert(Alert.AlertType.WARNING, "La matricola deve essere lunga 9 caratteri.").showAndWait();
            return;
        }
        if (!mail.toLowerCase().endsWith("@studenti.unina.it")) {
            new Alert(Alert.AlertType.WARNING, "Email non valida: usa un indirizzo @studenti.unina.it.").showAndWait();
            return;
        }
        if (!password.equals(confirm)) {
            new Alert(Alert.AlertType.WARNING, "Le password non coincidono.").showAndWait();
            return;
        }

        try (Connection conn = DB.getConnection()) {
            if (existsByColumn(conn, "matricola", matricola)) {
                new Alert(Alert.AlertType.WARNING, "Matricola gia registrata.").showAndWait();
                return;
            }
            if (existsByColumn(conn, "mail", mail)) {
                new Alert(Alert.AlertType.WARNING, "Email gia registrata.").showAndWait();
                return;
            }

            String sql = "INSERT INTO utente (matricola, nome, cognome, mail, password) VALUES (?, ?, ?, ?, ?)";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, matricola);
                ps.setString(2, nome);
                ps.setString(3, cognome);
                ps.setString(4, mail);
                String hash = BCrypt.hashpw(password, BCrypt.gensalt(12));
                ps.setString(5, hash);
                ps.executeUpdate();
            }
        } catch (Exception ex) {
            new Alert(Alert.AlertType.ERROR, "Errore durante la registrazione.").showAndWait();
            ex.printStackTrace();
            return;
        }

        new Alert(Alert.AlertType.INFORMATION, "Registrazione completata. Effettua il login.").showAndWait();
        onLoginLinkClick(event);
    }

    // Legge il testo del campo e lo normalizza.
    private String safeText(TextField field) {
        return field.getText() == null ? "" : field.getText().trim();
    }

    // Verifica l'esistenza di un valore univoco (matricola/mail).
    private boolean existsByColumn(Connection conn, String column, String value) throws Exception {
        String sql = "SELECT 1 FROM utente WHERE " + column + " = ? LIMIT 1";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, value);
            return ps.executeQuery().next();
        }
    }

}

