package com.cashflow.ui.view;

import com.cashflow.ui.service.CashFlowUiService;
import com.cashflow.ui.theme.ThemeManager;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.*;

import java.util.HashMap;
import java.util.Map;

public class MainLayout extends BorderPane {

    private final CashFlowUiService uiService;
    private final Map<String, Button> navButtons = new HashMap<>();
    private final StackPane contentArea = new StackPane();
    private String activeTab = "DASHBOARD";

    public MainLayout(CashFlowUiService uiService) {
        this.uiService = uiService;
        buildSidebar();
        setCenter(contentArea);
        navigateTo("DASHBOARD");
    }

    private void buildSidebar() {
        VBox sidebar = new VBox();
        sidebar.getStyleClass().add("sidebar");
        sidebar.setPrefWidth(220);

        // Logo
        VBox logoBox = new VBox(2);
        logoBox.setPadding(new Insets(0, 0, 16, 8));
        Label brand = new Label("CASH_IN");
        brand.getStyleClass().add("brand-title");
        Label sub = new Label("EARLY WARNING SYSTEM");
        sub.getStyleClass().add("brand-subtitle");
        logoBox.getChildren().addAll(brand, sub);

        sidebar.getChildren().add(logoBox);

        // Navigation Sections
        addSectionHeader(sidebar, "OVERVIEW");
        addNavItem(sidebar, "Dashboard", "DASHBOARD");

        addSectionHeader(sidebar, "ANALYZE");
        addNavItem(sidebar, "Transactions", "TRANSACTIONS");
        addNavItem(sidebar, "Forecast", "FORECAST");
        addNavItem(sidebar, "Warnings & Anomalies", "WARNINGS");
        addNavItem(sidebar, "Receivables", "RECEIVABLES");
        addNavItem(sidebar, "Communication", "COMMUNICATION");
        addNavItem(sidebar, "Expenses", "EXPENSES");

        addSectionHeader(sidebar, "INVESTIGATE");
        addNavItem(sidebar, "Versions & Changes", "VERSIONS");
        addNavItem(sidebar, "What-If Simulator", "SCENARIOS");

        addSectionHeader(sidebar, "DATA & SYSTEM");
        addNavItem(sidebar, "Upload CSV", "UPLOAD");
        addNavItem(sidebar, "Reports & Export", "REPORTS");
        addNavItem(sidebar, "Settings", "SETTINGS");

        setLeft(sidebar);
    }

    private void addSectionHeader(VBox container, String title) {
        Label l = new Label(title);
        l.getStyleClass().add("nav-section-title");
        container.getChildren().add(l);
    }

    private void addNavItem(VBox container, String text, String key) {
        Button btn = new Button(text);
        btn.getStyleClass().add("nav-button");
        btn.setMaxWidth(Double.MAX_VALUE);
        btn.setOnAction(e -> navigateTo(key));
        navButtons.put(key, btn);
        container.getChildren().add(btn);
    }

    public void navigateTo(String key) {
        this.activeTab = key;

        // Update nav button active classes
        navButtons.forEach((k, btn) -> {
            btn.getStyleClass().remove("active");
            if (k.equals(key)) {
                btn.getStyleClass().add("active");
            }
        });

        // Instantiate view dynamically based on tab key
        Node view;
        switch (key) {
            case "DASHBOARD":
                view = new DashboardView(uiService);
                break;
            case "TRANSACTIONS":
                view = new TransactionsView(uiService);
                break;
            case "FORECAST":
                view = new ForecastView(uiService);
                break;
            case "WARNINGS":
                view = new WarningsView(uiService);
                break;
            case "RECEIVABLES":
                view = new ReceivablesView(uiService);
                break;
            case "COMMUNICATION":
                view = new CommunicationView(uiService);
                break;
            case "EXPENSES":
                view = new ExpensesView(uiService);
                break;
            case "VERSIONS":
                view = new VersionsView(uiService);
                break;
            case "SCENARIOS":
                view = new ScenariosView(uiService);
                break;
            case "UPLOAD":
                view = new UploadView(uiService, () -> navigateTo("DASHBOARD"));
                break;
            case "REPORTS":
                view = new ReportsView(uiService);
                break;
            case "SETTINGS":
                view = new SettingsView(uiService);
                break;
            default:
                view = new DashboardView(uiService);
                break;
        }

        contentArea.getChildren().setAll(view);
    }
}
