package com.example.uninaswapoobd2425.controller;

import javafx.animation.FadeTransition;
import javafx.animation.ParallelTransition;
import javafx.animation.ScaleTransition;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Side;
import javafx.scene.Node;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Circle;
import javafx.beans.binding.Bindings;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.net.URL;
import java.util.Objects;
import java.util.ResourceBundle;

public class homepageController {
    @FXML
    private ImageView avatarImage;

    @FXML
    private StackPane avatarContainer;

    private ContextMenu profileMenu;

    @FXML
    public void initialize() {

        Image img = new Image(
                Objects.requireNonNull(
                        getClass().getResource("/com/example/uninaswapoobd2425/imgs/prov.png")
                ).toExternalForm()
        );

        avatarImage.setImage(img);

        // CLIP CIRCOLARE (robusto: segue le dimensioni dell'ImageView)
        Circle clip = new Circle();
        clip.centerXProperty().bind(avatarImage.fitWidthProperty().divide(2));
        clip.centerYProperty().bind(avatarImage.fitHeightProperty().divide(2));
        clip.radiusProperty().bind(
                Bindings.min(avatarImage.fitWidthProperty(), avatarImage.fitHeightProperty()).divide(2)
        );
        avatarImage.setClip(clip);

        profileMenu = new ContextMenu();

        MenuItem profile = new MenuItem("Profilo");
        MenuItem settings = new MenuItem("Impostazioni");
        MenuItem logout = new MenuItem("Logout");

        profileMenu.getItems().addAll(profile, settings, logout);
    }

    @FXML
    private void openProfileMenu(MouseEvent event) {
        event.consume();

        if (profileMenu.isShowing()) {
            profileMenu.hide();
            return;
        }

        profileMenu.show(avatarContainer, Side.BOTTOM, 0, 6);
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
}
