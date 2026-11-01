package com.cashflow.ui.view;

import com.cashflow.model.ForecastEntry;
import com.cashflow.ui.service.CashFlowUiService;
import javafx.geometry.Insets;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class ForecastView extends ScrollPane {

    private final CashFlowUiService uiService;
    private final ComboBox<Integer> daysCombo = new ComboBox<>();
    private final LineChart<String, Number> lineChart;
    private final VBox metricsBox = new VBox(10);
    private final NumberFormat currencyFormatter = NumberFormat.getCurrencyInstance(new Locale("en", "IN"));

    public ForecastView(CashFlowUiService uiService) {
        this.uiService = uiService;
        setFitToWidth(true);
        setStyle("-fx-background-color: transparent; -fx-background: transparent;");

        CategoryAxis xAxis = new CategoryAxis();
        xAxis.setLabel("Projection Date");
        NumberAxis yAxis = new NumberAxis();
        yAxis.setLabel("Cash Balance (₹)");
        lineChart = new LineChart<>(xAxis, yAxis);
        lineChart.setTitle("Projected Cash Trajectory");
        lineChart.setPrefHeight(400);

        buildUI();
        loadForecast(30);
    }

    private void buildUI() {
        VBox container = new VBox(20);
        container.setPadding(new Insets(24));

        VBox headerBox = new VBox(4);
        Label title = new Label("Predictive Cash-Flow Forecast");
        title.getStyleClass().add("header-title");
        Label subtitle = new Label("Simulation of future liquidity combining expected receivables, scheduled bills, and recurring payments.");
        subtitle.getStyleClass().add("header-subtitle");
        headerBox.getChildren().addAll(title, subtitle);

        HBox controlBar = new HBox(12);
        controlBar.getChildren().addAll(new Label("Forecast Period:"), daysCombo);
        daysCombo.getItems().addAll(7, 30, 60, 90);
        daysCombo.setValue(30);
        daysCombo.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) loadForecast(newVal);
        });

        HBox.setHgrow(lineChart, Priority.ALWAYS);

        container.getChildren().addAll(headerBox, controlBar, lineChart, metricsBox);
        setContent(container);
    }

    public void loadForecast(int days) {
        lineChart.getData().clear();
        metricsBox.getChildren().clear();

        List<ForecastEntry> forecast = uiService.getForecast(days);
        if (forecast.isEmpty()) {
            metricsBox.getChildren().add(new Label("No forecast data available. Please upload a dataset first."));
            return;
        }

        XYChart.Series<String, Number> closingCashSeries = new XYChart.Series<>();
        closingCashSeries.setName("Projected Cash Floor");

        BigDecimal minCash = null;
        BigDecimal endCash = null;

        for (int i = 0; i < forecast.size(); i++) {
            ForecastEntry e = forecast.get(i);
            // Display sample points to keep chart readable
            if (days > 30 && i % (days / 15) != 0 && i != forecast.size() - 1) continue;

            String label = e.getForecastDate().toString().substring(5); // MM-DD
            double closing = e.getClosingCash().doubleValue();
            closingCashSeries.getData().add(new XYChart.Data<>(label, closing));

            if (minCash == null || e.getClosingCash().compareTo(minCash) < 0) {
                minCash = e.getClosingCash();
            }
            endCash = e.getClosingCash();
        }

        lineChart.getData().add(closingCashSeries);

        HBox summary = new HBox(20);
        summary.getChildren().addAll(
                createBadge("Forecast Horizon", days + " Days"),
                createBadge("Projected Minimum Cash", formatAmount(minCash)),
                createBadge("Estimated Closing Cash", formatAmount(endCash))
        );
        metricsBox.getChildren().add(summary);
    }

    private VBox createBadge(String title, String val) {
        VBox b = new VBox(4);
        b.getStyleClass().add("kpi-card");
        b.setPrefWidth(220);
        Label t = new Label(title);
        t.getStyleClass().add("kpi-title");
        Label v = new Label(val);
        v.getStyleClass().add("kpi-value");
        b.getChildren().addAll(t, v);
        return b;
    }

    private String formatAmount(BigDecimal amount) {
        if (amount == null) return "₹0.00";
        return currencyFormatter.format(amount);
    }
}
