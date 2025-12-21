package com.example.uninaswapoobd2425.controller;

import javafx.animation.FadeTransition;
import javafx.animation.ParallelTransition;
import javafx.animation.ScaleTransition;
import javafx.beans.binding.Bindings;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Bounds;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.Popup;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.util.Objects;

public class homepageController {
    @FXML
    private ImageView avatarImage;

    @FXML
    private StackPane avatarContainer;

    private Popup profilePopup;
    private VBox menuContent;

    @FXML
    public void initialize() {
        Image img = new Image(
                Objects.requireNonNull(
                        getClass().getResource("/com/example/uninaswapoobd2425/imgs/prov.png")
                ).toExternalForm()
        );

        avatarImage.setImage(img);

        Circle clip = new Circle();
        clip.centerXProperty().bind(avatarImage.fitWidthProperty().divide(2));
        clip.centerYProperty().bind(avatarImage.fitHeightProperty().divide(2));
        clip.radiusProperty().bind(
                Bindings.min(avatarImage.fitWidthProperty(), avatarImage.fitHeightProperty()).divide(2)
        );
        avatarImage.setClip(clip);

        configureProfileMenu();

    }

    @FXML
    private void openProfileMenu(MouseEvent event) {
        event.consume();

        if (profilePopup.isShowing()) {
            hideProfileMenuWithAnimation();
            return;
        }

        Bounds avatarBounds = avatarContainer.localToScreen(avatarContainer.getBoundsInLocal());
        if (avatarBounds == null) {
            return;
        }
        double horizontalOffset = 70;
        double popupX = avatarBounds.getMaxX() - menuContent.getPrefWidth() + horizontalOffset;
        double popupY = avatarBounds.getMaxY();

        showProfileMenu(popupX, popupY);
    }

    private void configureProfileMenu() {
        menuContent = buildMenuContent();
        menuContent.getStylesheets().add(
                Objects.requireNonNull(
                        getClass().getResource("/com/example/uninaswapoobd2425/style.css")
                ).toExternalForm()
        );

        profilePopup = new Popup();
        profilePopup.setAutoHide(true);
        profilePopup.getContent().add(menuContent);
    }

    private VBox buildMenuContent() {
        VBox box = new VBox();
        box.setSpacing(0);
        box.getStyleClass().add("profile-menu");
        box.setPrefWidth(240);
        box.setFillWidth(true);

        Button dashboard = buildMenuButton("▦", "Dashboard", null, createStatusDot(), () -> {});
        Button account = buildMenuButton("👤", "Account", null, null, () -> {});
        Button settings = buildMenuButton("⚙️", "Settings", null, null, () -> {});
        Button logout = buildMenuButton("↺", "Log out", null, null, () -> {});
        logout.getStyleClass().add("profile-menu-item-last");

        box.getChildren().addAll(dashboard, account, settings, logout);
        return box;
    }

    private Button buildMenuButton(String iconText, String title, String subtitle, Node trailingNode, Runnable action) {
        Button button = new Button();
        button.getStyleClass().setAll("profile-menu-item");
        button.setText(null);
        button.setMaxWidth(Double.MAX_VALUE);
        button.setAlignment(Pos.CENTER_LEFT);

        Label icon = new Label(iconText);
        icon.getStyleClass().add("profile-menu-item-icon");

        Label titleLabel = new Label(title);
        titleLabel.getStyleClass().add("profile-menu-item-title");

        VBox textColumn = new VBox(2);
        textColumn.getChildren().add(titleLabel);
        textColumn.setAlignment(Pos.CENTER_LEFT);
        if (subtitle != null && !subtitle.isBlank()) {
            Label subtitleLabel = new Label(subtitle);
            subtitleLabel.getStyleClass().add("profile-menu-item-subtitle");
            textColumn.getChildren().add(subtitleLabel);
        }
        textColumn.getStyleClass().add("profile-menu-item-text");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox content = new HBox(12, icon, textColumn, spacer);
        content.getStyleClass().add("profile-menu-item-content");
        if (trailingNode != null) {
            content.getChildren().add(trailingNode);
        }

        button.setGraphic(content);
        button.setOnAction(e -> {
            if (action != null) {
                action.run();
            }
            hideProfileMenuWithAnimation();
        });
        return button;
    }

    private Separator createDivider() {
        Separator separator = new Separator();
        separator.getStyleClass().add("profile-menu-divider");
        return separator;
    }

    private Label createStatusDot() {
        Label dot = new Label();
        dot.getStyleClass().add("profile-menu-status-dot");
        return dot;
    }

    private Label createShortcutTag(String text) {
        Label shortcut = new Label(text);
        shortcut.getStyleClass().add("profile-menu-shortcut");
        return shortcut;
    }

    private void showProfileMenu(double screenX, double screenY) {
        menuContent.setOpacity(0);
        menuContent.setScaleX(0.92);
        menuContent.setScaleY(0.92);

        profilePopup.show(avatarContainer.getScene().getWindow(), screenX, screenY);

        FadeTransition fadeIn = new FadeTransition(Duration.millis(180), menuContent);
        fadeIn.setFromValue(0);
        fadeIn.setToValue(1);

        ScaleTransition scaleIn = new ScaleTransition(Duration.millis(180), menuContent);
        scaleIn.setFromX(0.92);
        scaleIn.setToX(1);
        scaleIn.setFromY(0.92);
        scaleIn.setToY(1);

        new ParallelTransition(fadeIn, scaleIn).play();
    }

    private void hideProfileMenuWithAnimation() {
        FadeTransition fadeOut = new FadeTransition(Duration.millis(140), menuContent);
        fadeOut.setFromValue(1);
        fadeOut.setToValue(0);

        ScaleTransition scaleOut = new ScaleTransition(Duration.millis(140), menuContent);
        scaleOut.setFromX(1);
        scaleOut.setToX(0.9);
        scaleOut.setFromY(1);
        scaleOut.setToY(0.9);

        ParallelTransition out = new ParallelTransition(fadeOut, scaleOut);
        out.setOnFinished(e -> profilePopup.hide());
        out.play();
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