package com.cashflow.ui;

import com.cashflow.ui.service.CashFlowUiService;
import com.cashflow.ui.service.MockUiService;
import com.cashflow.ui.theme.ThemeManager;
import com.cashflow.ui.view.MainLayout;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.util.Map;

/**
 * Entry point for the Independent Java Frontend.
 * Runs completely without Spring Boot, without SQLite, and without network backend.
 */
public class StandaloneFrontendApp extends Application {

    @Override
    public void start(Stage primaryStage) {
        // Use MockUiService - 100% decoupled from any backend
        CashFlowUiService mockService = new MockUiService();

        MainLayout root = new MainLayout(mockService);
        Scene scene = new Scene(root, 1200, 800);

        Map<String, Object> settings = mockService.getSettings();
        String theme = (String) settings.getOrDefault("theme", "arctic-ledger");
        ThemeManager.applyTheme(scene, theme);

        primaryStage.setTitle("CashFlow Independent Frontend [Pure Java Desktop - No Backend / No DB]");
        primaryStage.setScene(scene);
        primaryStage.setMinWidth(1000);
        primaryStage.setMinHeight(650);
        primaryStage.show();
    }

    @Override
    public void stop() {
        Platform.exit();
    }

    public static void main(String[] args) {
        launch(args);
 
   }
}
 