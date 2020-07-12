module MultiClipboard {
    requires javafx.fxml;
    requires javafx.controls;
    requires javafx.graphics;
    requires java.desktop;
    requires jnativehook;
    requires java.logging;
    requires com.google.gson;
    requires fuzzywuzzy;
    requires com.sun.jna;
    requires com.sun.jna.platform;

    opens sample;
}