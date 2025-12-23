package com.example.uninaswapoobd2425.controller;

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
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;

public class RegisterController {
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
    void handleClose(ActionEvent event) {
        javafx.application.Platform.exit();
    }

    @FXML
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

}

