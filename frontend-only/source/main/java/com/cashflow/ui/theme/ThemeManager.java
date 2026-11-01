package com.cashflow.ui.theme;

import javafx.scene.Scene;
import java.net.URL;

public class ThemeManager {

    private static String currentTheme = "arctic-ledger";

    public static void applyTheme(Scene scene, String themeName) {
        if (themeName == null || themeName.trim().isEmpty()) {
            themeName = "arctic-ledger";
        }
        currentTheme = themeName;
        scene.getStylesheets().clear();

        String cssFile = "/styles/theme-" + themeName.toLowerCase() + ".css";
        URL resource = ThemeManager.class.getResource(cssFile);
        if (resource != null) {
            scene.getStylesheets().add(resource.toExternalForm());
        } else {
            // fallback
            URL fallback = ThemeManager.class.getResource("/styles/theme-arctic-ledger.css");
            if (fallback != null) {
                scene.getStylesheets().add(fallback.toExternalForm());
            }
        }
    }

    public static String getCurrentTheme() {
        return currentTheme;
    }
}
