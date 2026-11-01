package com.cashflow.ui;

import com.cashflow.CashFlowApplication;
import com.cashflow.ui.service.CashFlowUiService;
import com.cashflow.ui.theme.ThemeManager;
import com.cashflow.ui.view.MainLayout;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;

import java.util.Map;

public class CashFlowDesktopApp extends Application {

    private ConfigurableApplicationContext springContext;

    @Override
    public void init() {
        // Boot Spring Boot context with embedded SQLite
        springContext = new SpringApplicationBuilder(CashFlowApplication.class)
                .headless(false)
                .run();
    }

    @Override
    public void start(Stage primaryStage) {
        CashFlowUiService uiService = springContext.getBean(CashFlowUiService.class);

        MainLayout root = new MainLayout(uiService);
        Scene scene = new Scene(root, 1200, 800);

        // Load saved theme
        Map<String, Object> settings = uiService.getSettings();
        String theme = (String) settings.getOrDefault("theme", "arctic-ledger");
        ThemeManager.applyTheme(scene, theme);

        primaryStage.setTitle("CashFlow Early Warning System (Java Desktop)");
        primaryStage.setScene(scene);
        primaryStage.setMinWidth(1000);
        primaryStage.setMinHeight(650);
        primaryStage.show();
    }

    @Override
    public void stop() {
        if (springContext != null) {
            springContext.close();
        }
        Platform.exit();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
