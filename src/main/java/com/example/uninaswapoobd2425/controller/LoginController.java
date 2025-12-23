package com.example.uninaswapoobd2425.controller;

import javafx.animation.FadeTransition;
import javafx.animation.ParallelTransition;
import javafx.animation.ScaleTransition;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.util.Duration;


import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.Statement;


public class LoginController {
    @FXML
    void handleClose(ActionEvent event) {
        javafx.application.Platform.exit();
    }

    @FXML
    private Pane rootPane;

    private double xOffset = 0;
    private double yOffset = 0;

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

}
