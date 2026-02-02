package com.example.uninaswapoobd2425.controller;

import com.example.uninaswapoobd2425.dao.DB;
import com.example.uninaswapoobd2425.dao.annuncioDAO;
import com.example.uninaswapoobd2425.dao.wishlistDAO;
import com.example.uninaswapoobd2425.model.annuncio;
import com.example.uninaswapoobd2425.model.categoriaAnnuncio;
import com.example.uninaswapoobd2425.model.Session;
import com.example.uninaswapoobd2425.model.tipoAnnuncio;
import javafx.animation.FadeTransition;
import javafx.animation.ParallelTransition;
import javafx.animation.ScaleTransition;
import javafx.beans.binding.Bindings;
import javafx.event.ActionEvent;
import javafx.scene.Scene;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Bounds;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.shape.Circle;
import javafx.stage.Popup;
import javafx.stage.Stage;
import javafx.util.Duration;
import javafx.scene.layout.Region;
import javafx.geometry.Pos;
import java.io.IOException;
import java.sql.Connection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class homepageController {
    @FXML
    private ImageView avatarImage;

    @FXML
    private StackPane avatarContainer;
    @FXML
    private TilePane tileAnnunci;
    @FXML
    private BorderPane mainBorderPane;

    @FXML
    private StackPane rootStackPane;
    @FXML
    private StackPane modalOverlay;

    @FXML private HBox menuVendita;
    @FXML private HBox menuScambio;
    @FXML private HBox menuRegalo;
    @FXML private HBox menuPreferiti;
    @FXML private HBox menuOfferte;
    @FXML private HBox menuRecensioni;
    @FXML private HBox menuStatistiche;

    @FXML private HBox filterBar;
    @FXML private ScrollPane scrollAnnunci;

    @FXML private ToggleButton btnTutti, btnLibri, btnInformatica, btnAbbigliamento, btnStrumenti, btnAltro;
    @FXML private Button btnExpand;

    private final ToggleGroup catGroup = new ToggleGroup();
    private boolean expanded = true;
    private final Map<ToggleButton, categoriaAnnuncio> map = new HashMap<>();
    private tipoAnnuncio tipoSelezionato = tipoAnnuncio.vendita;
    private categoriaAnnuncio categoriaSelezionata = null;
    private boolean preferitiAttivo = false;
    private boolean offerteAttivo = false;
    private boolean recensioniAttivo = false;
    private boolean statisticheAttivo = false;
    private Node annunciContent;

    private Popup profilePopup;
    private VBox menuContent;

    @FXML
    // Inizializza UI, filtri e carica la lista annunci.
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

        btnTutti.setToggleGroup(catGroup);
        btnLibri.setToggleGroup(catGroup);
        btnInformatica.setToggleGroup(catGroup);
        btnAbbigliamento.setToggleGroup(catGroup);
        btnStrumenti.setToggleGroup(catGroup);
        btnAltro.setToggleGroup(catGroup);

        btnTutti.setSelected(true);

        map.put(btnLibri, categoriaAnnuncio.libri);
        map.put(btnInformatica, categoriaAnnuncio.informatica);
        map.put(btnAbbigliamento, categoriaAnnuncio.abbigliamento);
        map.put(btnStrumenti, categoriaAnnuncio.strumenti_musicali);
        map.put(btnAltro, categoriaAnnuncio.altro);

        btnExpand.setOnAction(e -> toggleFilters());
        catGroup.selectedToggleProperty().addListener((obs, oldT, newT) -> {
            if (newT == null) {
                if (oldT != null) oldT.setSelected(true);
                return;
            }
            if (newT == btnTutti) filtraPerCategoria(null);
            else filtraPerCategoria(map.get((ToggleButton) newT));
        });

        btnTutti.setSelected(true);
        catGroup.selectToggle(btnTutti);

        annunciContent = scrollAnnunci.getContent();

        setPreferiti(false);
        setOfferte(false);
        setRecensioni(false);
        setStatistiche(false);
        setTipoSelezionato(tipoAnnuncio.vendita);
        caricaAnnunci(tipoSelezionato, categoriaSelezionata);
        configureProfileMenu();

        modalOverlay.setPickOnBounds(true);
        mainBorderPane.disableProperty().bind(modalOverlay.visibleProperty());
    }
    @FXML
    // Apre la modale per creare un nuovo annuncio.
    void handleNuovoAnnuncio(ActionEvent event) {
        try {

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/uninaswapoobd2425/nuovoAnnuncio.fxml"));
            Parent popupContent = loader.load();


            modalOverlay.getChildren().clear();
            modalOverlay.getChildren().add(popupContent);

            StackPane.setAlignment(popupContent, Pos.CENTER);

            if (popupContent instanceof Region) {
                ((Region) popupContent).setMaxSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);
            }
//            modalOverlay.setCursor(javafx.scene.Cursor.HAND);
//            popupContent.setCursor(javafx.scene.Cursor.DEFAULT);
            modalOverlay.setOnMouseClicked(mouseEvent -> {
                if(mouseEvent.getTarget() == modalOverlay){
                    modalOverlay.setVisible(false);
                    modalOverlay.getChildren().clear();
                }

            });


            modalOverlay.setVisible(true);
            modalOverlay.setManaged(true);
            modalOverlay.setMouseTransparent(false);

            // passare il riferimento a questo controller per chiudere il popup dopo:
            // NuovoAnnuncioController popupController = loader.getController();
            // popupController.setMainController(this);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Chiude la modale corrente.
    public void chiudiPopup() {
        modalOverlay.setVisible(false);
        modalOverlay.getChildren().clear();
    }

    // Espande/chiude i filtri di categoria.
    private void toggleFilters() {
        expanded = !expanded;

        setVisibleManaged(btnLibri, expanded);
        setVisibleManaged(btnInformatica, expanded);
        setVisibleManaged(btnAbbigliamento, expanded);
        setVisibleManaged(btnStrumenti, expanded);
        setVisibleManaged(btnAltro, expanded);

        btnExpand.setText(expanded ? "‹" : "›");
    }

    // Utility per gestire visibilita' e managed.
    private void setVisibleManaged(Control c, boolean v) {
        c.setVisible(v);
        c.setManaged(v);
    }

    // Applica filtro per categoria secondo lo stato corrente.
    private void filtraPerCategoria(categoriaAnnuncio cat) {
        categoriaSelezionata = cat;
        if (offerteAttivo) {
            caricaOfferte();
        } else if (recensioniAttivo) {
            caricaRecensioni();
        } else if (statisticheAttivo) {
            caricaStatistiche();
        } else if (preferitiAttivo) {
            caricaPreferiti(categoriaSelezionata);
        } else {
            caricaAnnunci(tipoSelezionato, categoriaSelezionata);
        }
    }

    // Renderizza una lista di annunci gia' pronta.
    public void loadAnnunci(List<annuncio> annunci) {
        tileAnnunci.getChildren().clear();

        try (Connection conn = DB.getConnection()) {
            wishlistDAO wDao = new wishlistDAO(conn);
            String matricola = Session.getMatricola();

            for (annuncio a : annunci) {
                try {
                    FXMLLoader loader = new FXMLLoader(getClass().getResource(
                            "/com/example/uninaswapoobd2425/annuncioCard.fxml"
                    ));
                    Parent card = loader.load();
                    annuncioCardController c = loader.getController();
                    boolean wishlisted = matricola != null && !matricola.isBlank() && wDao.exists(a.getIdAnnuncio(), matricola);
                    int wishlistCount = wDao.countForAnnuncio(a.getIdAnnuncio());
                    c.setData(a, wishlisted, wishlistCount);

                    tileAnnunci.getChildren().add(card);

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    // Carica annunci dal DB in base a tipo/categoria.
    private void caricaAnnunci(tipoAnnuncio tipo, categoriaAnnuncio categoria) {
        tileAnnunci.getChildren().clear();

        try (Connection conn = DB.getConnection()) {
            annuncioDAO dao = new annuncioDAO(conn);
            wishlistDAO wDao = new wishlistDAO(conn);
            String matricola = Session.getMatricola();
            List<annuncio> lista = categoria == null
                    ? dao.getAnnunciAttiviConImgPrincipaleByTipo(tipo)
                    : dao.getAnnunciAttiviConImgPrincipaleByTipoAndCategoria(tipo, categoria);

            for (annuncio a : lista) {
                FXMLLoader loader = new FXMLLoader(getClass().getResource(
                        "/com/example/uninaswapoobd2425/annuncioCard.fxml"
                ));
                Parent card = loader.load();
                card.setPickOnBounds(true);
                card.setCursor(javafx.scene.Cursor.HAND);
                card.setOnMouseClicked(e -> openDettaglio(a));
                card.setPickOnBounds(true);

                annuncioCardController c = loader.getController();
                boolean wishlisted = matricola != null && !matricola.isBlank() && wDao.exists(a.getIdAnnuncio(), matricola);
                int wishlistCount = wDao.countForAnnuncio(a.getIdAnnuncio());
                c.setData(a, wishlisted, wishlistCount);

                tileAnnunci.getChildren().add(card);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Carica preferiti dell'utente (con filtro categoria).
    private void caricaPreferiti(categoriaAnnuncio categoria) {
        tileAnnunci.getChildren().clear();

        String matricola = Session.getMatricola();
        if (matricola == null || matricola.isBlank()) {
            new Alert(Alert.AlertType.WARNING, "Devi essere loggato per vedere i preferiti.").showAndWait();
            return;
        }

        try (Connection conn = DB.getConnection()) {
            annuncioDAO dao = new annuncioDAO(conn);
            wishlistDAO wDao = new wishlistDAO(conn);
            List<annuncio> lista = categoria == null
                    ? dao.getAnnunciPreferitiByUtente(matricola)
                    : dao.getAnnunciPreferitiByUtenteAndCategoria(matricola, categoria);

            for (annuncio a : lista) {
                FXMLLoader loader = new FXMLLoader(getClass().getResource(
                        "/com/example/uninaswapoobd2425/annuncioCard.fxml"
                ));
                Parent card = loader.load();
                card.setPickOnBounds(true);
                card.setCursor(javafx.scene.Cursor.HAND);
                card.setOnMouseClicked(e -> openDettaglio(a));

                annuncioCardController c = loader.getController();
                int wishlistCount = wDao.countForAnnuncio(a.getIdAnnuncio());
                c.setData(a, true, wishlistCount, true);

                tileAnnunci.getChildren().add(card);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    // Filtro rapido: vendita.
    private void handleFilterVendita(MouseEvent event) {
        setOfferte(false);
        setRecensioni(false);
        setStatistiche(false);
        setPreferiti(false);
        setTipoSelezionato(tipoAnnuncio.vendita);
        caricaAnnunci(tipoSelezionato, categoriaSelezionata);
    }

    @FXML
    // Filtro rapido: scambio.
    private void handleFilterScambio(MouseEvent event) {
        setOfferte(false);
        setRecensioni(false);
        setStatistiche(false);
        setPreferiti(false);
        setTipoSelezionato(tipoAnnuncio.scambio);
        caricaAnnunci(tipoSelezionato, categoriaSelezionata);
    }

    @FXML
    // Filtro rapido: regalo.
    private void handleFilterRegalo(MouseEvent event) {
        setOfferte(false);
        setRecensioni(false);
        setStatistiche(false);
        setPreferiti(false);
        setTipoSelezionato(tipoAnnuncio.regalo);
        caricaAnnunci(tipoSelezionato, categoriaSelezionata);
    }

    @FXML
    // Mostra i preferiti.
    private void handleFilterPreferiti(MouseEvent event) {
        setOfferte(false);
        setRecensioni(false);
        setStatistiche(false);
        setPreferiti(true);
        caricaPreferiti(categoriaSelezionata);
    }

    @FXML
    // Mostra le offerte.
    private void handleFilterOfferte(MouseEvent event) {
        setPreferiti(false);
        setRecensioni(false);
        setStatistiche(false);
        setOfferte(true);
        caricaOfferte();
    }

    @FXML
    // Mostra le recensioni.
    private void handleFilterRecensioni(MouseEvent event) {
        setPreferiti(false);
        setOfferte(false);
        setStatistiche(false);
        setRecensioni(true);
        caricaRecensioni();
    }

    @FXML
    // Mostra le statistiche.
    private void handleFilterStatistiche(MouseEvent event) {
        setPreferiti(false);
        setOfferte(false);
        setRecensioni(false);
        setStatistiche(true);
        caricaStatistiche();
    }

    // Imposta il tipo selezionato e aggiorna menu.
    private void setTipoSelezionato(tipoAnnuncio tipo) {
        tipoSelezionato = tipo;
        setMenuActive(menuVendita, tipo == tipoAnnuncio.vendita);
        setMenuActive(menuScambio, tipo == tipoAnnuncio.scambio);
        setMenuActive(menuRegalo, tipo == tipoAnnuncio.regalo);
    }

    // Applica classe CSS attiva a un menu.
    private void setMenuActive(HBox box, boolean active) {
        if (box == null) return;
        String base = "menu-btn";
        if (active) {
            box.getStyleClass().setAll(base, "menu-btn-active");
        } else {
            box.getStyleClass().setAll(base);
        }
    }

    // Attiva/disattiva modalita preferiti.
    private void setPreferiti(boolean attivo) {
        preferitiAttivo = attivo;
        setMenuActive(menuPreferiti, attivo);
        if (attivo) {
            setMenuActive(menuVendita, false);
            setMenuActive(menuScambio, false);
            setMenuActive(menuRegalo, false);
        }
    }

    // Attiva/disattiva modalita offerte e gestisce contenuto.
    private void setOfferte(boolean attivo) {
        offerteAttivo = attivo;
        setMenuActive(menuOfferte, attivo);
        filterBar.setVisible(!attivo && !recensioniAttivo && !statisticheAttivo);
        filterBar.setManaged(!attivo && !recensioniAttivo && !statisticheAttivo);
        if (!attivo && !recensioniAttivo && !statisticheAttivo && scrollAnnunci.getContent() != annunciContent) {
            scrollAnnunci.setContent(annunciContent);
        }
        if (attivo) {
            setMenuActive(menuVendita, false);
            setMenuActive(menuScambio, false);
            setMenuActive(menuRegalo, false);
            setMenuActive(menuPreferiti, false);
            setMenuActive(menuRecensioni, false);
            setMenuActive(menuStatistiche, false);
        }
    }

    // Attiva/disattiva modalita recensioni e gestisce contenuto.
    private void setRecensioni(boolean attivo) {
        recensioniAttivo = attivo;
        setMenuActive(menuRecensioni, attivo);
        filterBar.setVisible(!attivo && !offerteAttivo && !statisticheAttivo);
        filterBar.setManaged(!attivo && !offerteAttivo && !statisticheAttivo);
        if (!attivo && !offerteAttivo && !statisticheAttivo && scrollAnnunci.getContent() != annunciContent) {
            scrollAnnunci.setContent(annunciContent);
        }
        if (attivo) {
            setMenuActive(menuVendita, false);
            setMenuActive(menuScambio, false);
            setMenuActive(menuRegalo, false);
            setMenuActive(menuPreferiti, false);
            setMenuActive(menuOfferte, false);
            setMenuActive(menuStatistiche, false);
        }
    }

    // Attiva/disattiva modalita statistiche e gestisce contenuto.
    private void setStatistiche(boolean attivo) {
        statisticheAttivo = attivo;
        setMenuActive(menuStatistiche, attivo);
        filterBar.setVisible(!attivo && !offerteAttivo && !recensioniAttivo);
        filterBar.setManaged(!attivo && !offerteAttivo && !recensioniAttivo);
        if (!attivo && !offerteAttivo && !recensioniAttivo && scrollAnnunci.getContent() != annunciContent) {
            scrollAnnunci.setContent(annunciContent);
        }
        if (attivo) {
            setMenuActive(menuVendita, false);
            setMenuActive(menuScambio, false);
            setMenuActive(menuRegalo, false);
            setMenuActive(menuPreferiti, false);
            setMenuActive(menuOfferte, false);
            setMenuActive(menuRecensioni, false);
        }
    }

    // Carica view offerte e la inserisce nello scroll.
    private void caricaOfferte() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(
                    "/com/example/uninaswapoobd2425/offerte.fxml"
            ));
            Parent view = loader.load();
            offerteController c = loader.getController();
            c.loadData();
            scrollAnnunci.setContent(view);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    // Carica view recensioni e la inserisce nello scroll.
    private void caricaRecensioni() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(
                    "/com/example/uninaswapoobd2425/recensioni.fxml"
            ));
            Parent view = loader.load();
            recensioniController c = loader.getController();
            c.loadData();
            scrollAnnunci.setContent(view);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    // Carica view statistiche e la inserisce nello scroll.
    private void caricaStatistiche() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(
                    "/com/example/uninaswapoobd2425/statistiche.fxml"
            ));
            Parent view = loader.load();
            statisticheController c = loader.getController();
            c.loadData();
            scrollAnnunci.setContent(view);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    // Apre la modale con il dettaglio annuncio.
    private void openDettaglio(annuncio a) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(
                    "/com/example/uninaswapoobd2425/dettaglioAnnuncio.fxml"
            ));
            Parent view = loader.load();

            if (view instanceof Region r) {
                r.setMaxSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);
            }

            modalOverlay.getChildren().setAll(view);
            StackPane.setAlignment(view, Pos.CENTER);

            modalOverlay.setVisible(true);
            dettaglioAnnuncioController c = loader.getController();
            c.setAnnuncio(a);
            c.setOnClose(this::closeModal);

            modalOverlay.getChildren().setAll(view);
            modalOverlay.setVisible(true);
            modalOverlay.setManaged(true);
            modalOverlay.setMouseTransparent(false);

            // click sullo sfondo per chiudere
            modalOverlay.setOnMouseClicked(ev -> {
                if (ev.getTarget() == modalOverlay) closeModal();
            });

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    // Chiude la modale di dettaglio.
    private void closeModal() {
        modalOverlay.getChildren().clear();
        modalOverlay.setVisible(false);
        modalOverlay.setManaged(false);
        modalOverlay.setMouseTransparent(true);
    }

    @FXML
    // Apre o chiude il menu profilo con animazione.
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

    // Prepara il popup del menu profilo.
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

    // Costruisce il contenuto del menu profilo.
    private VBox buildMenuContent() {
        VBox box = new VBox();
        box.setSpacing(0);
        box.getStyleClass().add("profile-menu");
        box.setPrefWidth(240);
        box.setFillWidth(true);

        Button dashboard = buildMenuButton("▦", "Homepage", null, createStatusDot(), () -> {});
        Button account = buildMenuButton("👤", "Account (W.I.P)", null, null, () -> {});
        Button settings = buildMenuButton("⚙️", "Settings (W.I.P)", null, null, () -> {});
        Button logout = buildMenuButton("↺", "Log out", null, null, this::handleLogout);
        logout.getStyleClass().add("profile-menu-item-last");

        box.getChildren().addAll(dashboard, account, settings, logout);
        return box;
    }

    // Esegue il logout e ritorna alla schermata login.
    private void handleLogout() {
        try {
            Session.clear();
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/uninaswapoobd2425/login.fxml"));
            Parent loginRoot = loader.load();
            Stage stage = (Stage) rootStackPane.getScene().getWindow();
            Scene scene = new Scene(loginRoot);
            scene.setFill(javafx.scene.paint.Color.TRANSPARENT);
            stage.setScene(scene);
            javafx.geometry.Rectangle2D bounds = javafx.stage.Screen.getPrimary().getVisualBounds();
            stage.sizeToScene();
            stage.setX(bounds.getMinX() + (bounds.getWidth() - stage.getWidth()) / 2);
            stage.setY(bounds.getMinY() + (bounds.getHeight() - stage.getHeight()) / 2);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    // Crea un bottone del menu profilo con icona e callback.
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

    // Crea un separatore di menu (se necessario).
    private Separator createDivider() {
        Separator separator = new Separator();
        separator.getStyleClass().add("profile-menu-divider");
        return separator;
    }

    // Crea il puntino di stato nel menu profilo.
    private Label createStatusDot() {
        Label dot = new Label();
        dot.getStyleClass().add("profile-menu-status-dot");
        return dot;
    }

    // Crea un tag di scorciatoia (non usato al momento).
    private Label createShortcutTag(String text) {
        Label shortcut = new Label(text);
        shortcut.getStyleClass().add("profile-menu-shortcut");
        return shortcut;
    }

    // Mostra il menu profilo con animazione.
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

    // Nasconde il menu profilo con animazione.
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
}

