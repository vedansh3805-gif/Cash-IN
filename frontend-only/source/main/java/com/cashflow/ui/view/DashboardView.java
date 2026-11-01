package com.cashflow.ui.view;

import com.cashflow.model.dto.DashboardMetrics;
import com.cashflow.ui.service.CashFlowUiService;
import javafx.geometry.Insets;
import javafx.scene.chart.*;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.*;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.Locale;

public class DashboardView extends ScrollPane {

    private final CashFlowUiService uiService;
    private final NumberFormat currencyFormatter = NumberFormat.getCurrencyInstance(new Locale("en", "IN"));

    public DashboardView(CashFlowUiService uiService) {
        this.uiService = uiService;
        setFitToWidth(true);
        setStyle("-fx-background-color: transparent; -fx-background: transparent;");
        buildUI();
    }

    private void buildUI() {
        VBox container = new VBox(20);
        container.setPadding(new Insets(24));

        // 1. Title Header
        VBox headerBox = new VBox(4);
        Label title = new Label("Financial Health & Cash-Flow Dashboard");
        title.getStyleClass().add("header-title");
        Label subtitle = new Label("Real-time cash metrics, liquidity projections and early risk indicators.");
        subtitle.getStyleClass().add("header-subtitle");
        headerBox.getChildren().addAll(title, subtitle);

        // 2. Fetch Metrics
        DashboardMetrics m = uiService.getDashboardMetrics();
        if (m == null) {
            m = new DashboardMetrics();
            m.setCurrentCash(BigDecimal.ZERO);
            m.setExpectedIncoming(BigDecimal.ZERO);
            m.setUpcomingExpenses(BigDecimal.ZERO);
            m.setNetCashFlow(BigDecimal.ZERO);
            m.setRiskStatus("LOW");
        }

        // 3. KPI Cards Grid
        GridPane grid = new GridPane();
        grid.setHgap(16);
        grid.setVgap(16);

        grid.add(createCard("Current Cash", formatAmount(m.getCurrentCash()), "Realized in-bank liquidity", false), 0, 0);
        grid.add(createCard("Expected Inflow", formatAmount(m.getExpectedIncoming()), "Pending & due receivables", false), 1, 0);
        grid.add(createCard("Upcoming Expenses", formatAmount(m.getUpcomingExpenses()), "Pending invoices & recurring", false), 2, 0);
        grid.add(createCard("Net Cash Flow", formatAmount(m.getNetCashFlow()), "Inflow minus outflow", false), 3, 0);

        grid.add(createCard("Pending Receivables", formatAmount(m.getPendingReceivables()), m.getPendingReceivablesCount() + " invoices pending", false), 0, 1);
        grid.add(createCard("Overdue Receivables", formatAmount(m.getOverdueReceivables()), m.getOverdueCount() + " invoices delayed", true), 1, 1);
        grid.add(createCard("Forecasted Min Cash", formatAmount(m.getForecastedMinCash()), "Next 90-day projection floor", false), 2, 1);
        grid.add(createCard("Risk Assessment", m.getRiskStatus() != null ? m.getRiskStatus() : "LOW", "Early warning threshold check", false), 3, 1);

        for (int i = 0; i < 4; i++) {
            ColumnConstraints col = new ColumnConstraints();
            col.setPercentWidth(25);
            grid.getColumnConstraints().add(col);
        }

        // 4. Charts Section
        HBox chartsBox = new HBox(20);
        chartsBox.setPrefHeight(340);

        // Chart 1: Cash Flow Overview (BarChart)
        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis yAxis = new NumberAxis();
        BarChart<String, Number> barChart = new BarChart<>(xAxis, yAxis);
        barChart.setTitle("Current Cash Dynamics (₹)");
        barChart.setLegendVisible(false);
        HBox.setHgrow(barChart, Priority.ALWAYS);

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        XYChart.Data<String, Number> d1 = new XYChart.Data<>("Current Cash", m.getCurrentCash() != null ? m.getCurrentCash().doubleValue() : 0);
        XYChart.Data<String, Number> d2 = new XYChart.Data<>("Incoming", m.getExpectedIncoming() != null ? m.getExpectedIncoming().doubleValue() : 0);
        XYChart.Data<String, Number> d3 = new XYChart.Data<>("Expenses", m.getUpcomingExpenses() != null ? m.getUpcomingExpenses().doubleValue() : 0);
        XYChart.Data<String, Number> d4 = new XYChart.Data<>("Net", m.getNetCashFlow() != null ? m.getNetCashFlow().doubleValue() : 0);

        series.getData().addAll(d1, d2, d3, d4);
        barChart.getData().add(series);

        // Assign beautiful semantic colors: Blue (Current Cash), Emerald Green (Incoming), Crimson/Rose (Expenses), Indigo (Net)
        javafx.application.Platform.runLater(() -> {
            if (d1.getNode() != null) d1.getNode().setStyle("-fx-bar-fill: #0ea5e9; -fx-background-radius: 4 4 0 0;");
            if (d2.getNode() != null) d2.getNode().setStyle("-fx-bar-fill: #10b981; -fx-background-radius: 4 4 0 0;");
            if (d3.getNode() != null) d3.getNode().setStyle("-fx-bar-fill: #f43f5e; -fx-background-radius: 4 4 0 0;");
            if (d4.getNode() != null) d4.getNode().setStyle("-fx-bar-fill: #8b5cf6; -fx-background-radius: 4 4 0 0;");
        });

        // Chart 2: Receivables Risk Ratio
        PieChart pieChart = new PieChart();
        pieChart.setTitle("Receivables Aging");
        double pending = m.getPendingReceivables() != null ? m.getPendingReceivables().doubleValue() : 0;
        double overdue = m.getOverdueReceivables() != null ? m.getOverdueReceivables().doubleValue() : 0;
        if (pending > 0 || overdue > 0) {
            pieChart.getData().add(new PieChart.Data("Pending On-Time", pending));
            pieChart.getData().add(new PieChart.Data("Overdue", overdue));
        } else {
            pieChart.getData().add(new PieChart.Data("No Outstanding", 100));
        }
        HBox.setHgrow(pieChart, Priority.ALWAYS);

        chartsBox.getChildren().addAll(barChart, pieChart);

        container.getChildren().addAll(headerBox, grid, chartsBox);
        setContent(container);
    }

    private VBox createCard(String titleText, String valueText, String noteText, boolean isAlert) {
        VBox card = new VBox(6);
        card.getStyleClass().add("kpi-card");

        Label title = new Label(titleText);
        title.getStyleClass().add("kpi-title");

        Label value = new Label(valueText != null ? valueText : "₹0");
        value.getStyleClass().add("kpi-value");
        if (isAlert) {
            value.setStyle("-fx-text-fill: -color-danger;");
        }

        Label note = new Label(noteText);
        note.setStyle("-fx-text-fill: -color-text-secondary; -fx-font-size: 11px;");

        card.getChildren().addAll(title, value, note);
        return card;
    }

    private String formatAmount(BigDecimal amount) {
        if (amount == null) return "₹0.00";
        return currencyFormatter.format(amount);
    }
}
