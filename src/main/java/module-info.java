module com.example.uninaswapoobd2425 {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.web;

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires net.synedra.validatorfx;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.bootstrapfx.core;
    requires eu.hansolo.tilesfx;
    requires com.almasb.fxgl.all;

    opens com.example.uninaswapoobd2425 to javafx.fxml;
    exports com.example.uninaswapoobd2425;
    exports com.example.uninaswapoobd2425.controller;
    opens com.example.uninaswapoobd2425.controller to javafx.fxml;
}