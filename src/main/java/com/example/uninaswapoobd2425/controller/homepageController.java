package com.example.uninaswapoobd2425.controller;

import javafx.fxml.FXML;
import javafx.geometry.Side;
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

}
