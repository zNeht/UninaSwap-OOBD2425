package com.example.uninaswapoobd2425.controller;

import com.example.uninaswapoobd2425.dao.DB;
import com.example.uninaswapoobd2425.model.Session;
import javafx.animation.FadeTransition;
import javafx.animation.ParallelTransition;
import javafx.animation.ScaleTransition;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.util.Duration;


import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import org.mindrot.jbcrypt.BCrypt;


public class loginController {
    @FXML
    // Chiude l'applicazione.
    void handleClose(ActionEvent event) {
        javafx.application.Platform.exit();
    }

    @FXML
    private Pane rootPane;
    @FXML
    private TextField matricolaField;
    @FXML
    private PasswordField passwordField;

    private double xOffset = 0;
    private double yOffset = 0;

    // Inizializza drag finestra e tenta auto-login.
    public void initialize() {
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
        autoLoginIfRemembered();
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
    // Transizione verso la schermata di registrazione.
    protected void onRegisterLinkClick(ActionEvent event) {

        Node sourceNode = (Node) event.getSource();
        Scene currentScene = sourceNode.getScene();
        Parent root = currentScene.getRoot();

        FadeTransition fadeOut = new FadeTransition(Duration.millis(300), root);
        fadeOut.setFromValue(1.0);
        fadeOut.setToValue(0.0);


        fadeOut.setOnFinished(e -> {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/uninaswapoobd2425/register.fxml"));
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
    // Esegue il login verificando le credenziali sul DB.
    void handleLogin(ActionEvent event) {
        String matricola = matricolaField.getText() != null ? matricolaField.getText().trim() : "";
        String password = passwordField.getText() != null ? passwordField.getText().trim() : "";

        if (matricola.isEmpty() || password.isEmpty()) {
            new Alert(Alert.AlertType.WARNING, "Inserisci matricola e password.").showAndWait();
            return;
        }

        String sql = "SELECT matricola, password FROM utente WHERE matricola = ?";
        try (Connection conn = DB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, matricola);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String hash = rs.getString("password");
                    if (hash != null && BCrypt.checkpw(password, hash)) {
                        Session.setMatricola(rs.getString("matricola"));
                        openHomepage(event);
                        return;
                    }
                }
            }
        } catch (Exception ex) {
            new Alert(Alert.AlertType.ERROR, "Errore durante il login.").showAndWait();
            ex.printStackTrace();
            return;
        }

        new Alert(Alert.AlertType.WARNING, "Credenziali non valide.").showAndWait();
    }

    // Apre la homepage usando l'evento come origine.
    private void openHomepage(ActionEvent event) {
        Node sourceNode = (Node) event.getSource();
        openHomepage(sourceNode.getScene());
    }

    // Apre la homepage con transizione dal scene corrente.
    private void openHomepage(Scene currentScene) {
        if (currentScene == null) return;
        Parent root = currentScene.getRoot();

        FadeTransition fadeOut = new FadeTransition(Duration.millis(250), root);
        fadeOut.setFromValue(1.0);
        fadeOut.setToValue(0.0);

        fadeOut.setOnFinished(e -> {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/uninaswapoobd2425/homepage.fxml"));
                Parent newRoot = loader.load();
                newRoot.setOpacity(0);

                Stage stage = (Stage) currentScene.getWindow();
                Scene newScene = new Scene(newRoot);
                newScene.setFill(Color.TRANSPARENT);
                stage.setScene(newScene);
                attachWindowDrag(stage, newRoot);
                centerStageOnScreen(stage);

                FadeTransition fadeIn = new FadeTransition(Duration.millis(250), newRoot);
                fadeIn.setFromValue(0.0);
                fadeIn.setToValue(1.0);
                fadeIn.play();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        });

        fadeOut.play();
    }

    // Se esiste una matricola in sessione, apre direttamente la homepage.
    private void autoLoginIfRemembered() {
        if (rootPane == null) return;
        Platform.runLater(() -> {
            String saved = Session.getMatricola();
            if (saved != null && !saved.isBlank()) {
                openHomepage(rootPane.getScene());
            }
        });
    }

    // Abilita il drag della finestra dalla root.
    private void attachWindowDrag(Stage stage, Parent root) {
        root.setOnMousePressed(event -> {
            xOffset = event.getSceneX();
            yOffset = event.getSceneY();
        });

        root.setOnMouseDragged(event -> {
            stage.setX(event.getScreenX() - xOffset);
            stage.setY(event.getScreenY() - yOffset);
        });
    }

    // Centra lo stage sullo schermo principale.
    private void centerStageOnScreen(Stage stage) {
        javafx.geometry.Rectangle2D bounds = Screen.getPrimary().getVisualBounds();
        stage.setX(bounds.getMinX() + (bounds.getWidth() - stage.getWidth()) / 2);
        stage.setY(bounds.getMinY() + (bounds.getHeight() - stage.getHeight()) / 2);
    }

}
