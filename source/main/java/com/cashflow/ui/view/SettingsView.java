package com.cashflow.ui.view;

import com.cashflow.ui.service.CashFlowUiService;
import com.cashflow.ui.theme.ThemeManager;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;

import java.math.BigDecimal;
import java.util.Map;

public class SettingsView extends VBox {

    private final CashFlowUiService uiService;
    private final ComboBox<String> themeCombo = new ComboBox<>();
    private final TextField thresholdField = new TextField();
    private final ComboBox<Integer> daysCombo = new ComboBox<>();
    private final TextField currencyField = new TextField();
    private final TextField shopkeeperPhoneField = new TextField();
    private final TextField shopkeeperEmailField = new TextField();
    private final CheckBox penaltyEnabledBox = new CheckBox("Enable Late Payment Penalties");
    private final ComboBox<String> penaltyTypeCombo = new ComboBox<>();
    private final TextField penaltyRateField = new TextField();
    private final TextField graceDaysField = new TextField();
    private final TextField maxPenaltyField = new TextField();
    private final Label statusLabel = new Label();

    public SettingsView(CashFlowUiService uiService) {
        this.uiService = uiService;
        setSpacing(20);
        setPadding(new Insets(24));
        buildUI();
        loadSettings();
    }

    private void buildUI() {
        VBox headerBox = new VBox(4);
        Label title = new Label("System Settings & Shopkeeper Profile");
        title.getStyleClass().add("header-title");
        Label subtitle = new Label("Configure visual themes, minimum cash thresholds, and shopkeeper contact info for dual-party SMS and Email alerts.");
        subtitle.getStyleClass().add("header-subtitle");
        headerBox.getChildren().addAll(title, subtitle);

        GridPane grid = new GridPane();
        grid.setHgap(16);
        grid.setVgap(16);
        grid.getStyleClass().add("kpi-card");
        grid.setPrefWidth(600);

        themeCombo.getItems().addAll("arctic-ledger", "forest-ledger", "copper-desk", "ocean-glass", "paper-ink");
        themeCombo.valueProperty().addListener((obs, oldV, newV) -> {
            if (newV != null && getScene() != null) {
                ThemeManager.applyTheme(getScene(), newV);
            }
        });
        daysCombo.getItems().addAll(7, 30, 60, 90);

        grid.add(new Label("Visual Theme:"), 0, 0);
        grid.add(themeCombo, 1, 0);

        grid.add(new Label("Min Cash Threshold (₹):"), 0, 1);
        grid.add(thresholdField, 1, 1);

        grid.add(new Label("Default Forecast Days:"), 0, 2);
        grid.add(daysCombo, 1, 2);

        grid.add(new Label("Currency Code:"), 0, 3);
        grid.add(currencyField, 1, 3);

        grid.add(new Label("Shopkeeper Phone (Caller/SMS):"), 0, 4);
        shopkeeperPhoneField.setPromptText("+91 98765 00000");
        grid.add(shopkeeperPhoneField, 1, 4);

        grid.add(new Label("Shopkeeper Email (Audit/Copy):"), 0, 5);
        shopkeeperEmailField.setPromptText("shopkeeper@cashflow.com");
        grid.add(shopkeeperEmailField, 1, 5);

        penaltyTypeCombo.getItems().addAll("DAILY_PERCENT", "PERCENTAGE", "FLAT", "DAILY_FLAT");
        grid.add(new Label("Late Penalty Policy:"), 0, 6);
        grid.add(penaltyEnabledBox, 1, 6);

        grid.add(new Label("Penalty Model:"), 0, 7);
        grid.add(penaltyTypeCombo, 1, 7);

        grid.add(new Label("Penalty Rate (% or Flat):"), 0, 8);
        grid.add(penaltyRateField, 1, 8);

        grid.add(new Label("Grace Period (Days):"), 0, 9);
        grid.add(graceDaysField, 1, 9);

        grid.add(new Label("Max Penalty Cap (%):"), 0, 10);
        grid.add(maxPenaltyField, 1, 10);

        Button saveBtn = new Button("Save Settings");
        saveBtn.getStyleClass().add("button-primary");
        saveBtn.setOnAction(e -> saveSettings());

        grid.add(saveBtn, 1, 11);

        statusLabel.setStyle("-fx-text-fill: -color-accent; -fx-font-weight: bold;");

        getChildren().addAll(headerBox, grid, statusLabel);
    }

    private void loadSettings() {
        Map<String, Object> s = uiService.getSettings();
        themeCombo.setValue(String.valueOf(s.getOrDefault("theme", "arctic-ledger")));
        thresholdField.setText(String.valueOf(s.getOrDefault("minimumCashThreshold", "50000")));
        daysCombo.setValue((Integer) s.getOrDefault("forecastDays", 90));
        currencyField.setText(String.valueOf(s.getOrDefault("currency", "INR")));
        shopkeeperPhoneField.setText(String.valueOf(s.getOrDefault("shopkeeperPhone", "+91 98765 00000")));
        shopkeeperEmailField.setText(String.valueOf(s.getOrDefault("shopkeeperEmail", "shopkeeper@cashflow.com")));
        Object pe = s.getOrDefault("penaltyEnabled", true);
        penaltyEnabledBox.setSelected(pe instanceof Boolean b ? b : Boolean.parseBoolean(String.valueOf(pe)));
        penaltyTypeCombo.setValue(String.valueOf(s.getOrDefault("penaltyType", "DAILY_PERCENT")));
        penaltyRateField.setText(String.valueOf(s.getOrDefault("penaltyRate", "0.1")));
        graceDaysField.setText(String.valueOf(s.getOrDefault("gracePeriodDays", "3")));
        maxPenaltyField.setText(String.valueOf(s.getOrDefault("maxPenaltyPercent", "25.0")));
    }

    private void saveSettings() {
        try {
            String theme = themeCombo.getValue();
            BigDecimal threshold = new BigDecimal(thresholdField.getText().trim());
            int days = daysCombo.getValue();
            String currency = currencyField.getText().trim();
            String phone = shopkeeperPhoneField.getText().trim();
            String email = shopkeeperEmailField.getText().trim();

            uiService.updateSettings(theme, days, threshold, currency, phone, email);
            uiService.updatePenaltySettings(
                    penaltyEnabledBox.isSelected(),
                    penaltyTypeCombo.getValue() != null ? penaltyTypeCombo.getValue() : "DAILY_PERCENT",
                    new BigDecimal(penaltyRateField.getText().trim()),
                    Integer.parseInt(graceDaysField.getText().trim()),
                    new BigDecimal(maxPenaltyField.getText().trim())
            );
            ThemeManager.applyTheme(getScene(), theme);
            statusLabel.setText("Settings saved successfully! Contact profile, penalty rules, and theme updated.");
        } catch (Exception ex) {
            statusLabel.setText("Error saving settings: " + ex.getMessage());
        }
    }
}
