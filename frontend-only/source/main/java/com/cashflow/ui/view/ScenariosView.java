package com.cashflow.ui.view;

import com.cashflow.model.ForecastEntry;
import com.cashflow.ui.service.CashFlowUiService;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public class ScenariosView extends ScrollPane {

    private final CashFlowUiService uiService;
    private final TextField nameField = new TextField("Delayed Payments Test");
    private final ComboBox<String> adjTypeCombo = new ComboBox<>();
    private final TextField amountField = new TextField("50000");
    private final TextField delayField = new TextField("15");
    private final Button runButton = new Button("Run What-If Simulation");
    private final LineChart<String, Number> compChart;
    private final Label resultSummary = new Label("Configure parameters above and click 'Run What-If Simulation'.");

    public ScenariosView(CashFlowUiService uiService) {
        this.uiService = uiService;
        setFitToWidth(true);
        setStyle("-fx-background-color: transparent; -fx-background: transparent;");

        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis yAxis = new NumberAxis();
        compChart = new LineChart<>(xAxis, yAxis);
        compChart.setTitle("Baseline Forecast vs Hypothetical Scenario");
        compChart.setPrefHeight(380);

        buildUI();
    }

    private void buildUI() {
        VBox container = new VBox(20);
        container.setPadding(new Insets(24));

        VBox headerBox = new VBox(4);
        Label title = new Label("What-If Cash-Flow Simulator");
        title.getStyleClass().add("header-title");
        Label subtitle = new Label("Test stress scenarios (delayed customer collections, unexpected expenses, revenue shocks) without touching actual data.");
        subtitle.getStyleClass().add("header-subtitle");
        headerBox.getChildren().addAll(title, subtitle);

        // Controls Grid
        GridPane grid = new GridPane();
        grid.setHgap(16);
        grid.setVgap(12);
        grid.getStyleClass().add("kpi-card");

        adjTypeCombo.getItems().addAll("DELAY_RECEIVABLES", "INCREASE_EXPENSES", "UNEXPECTED_EXPENSE", "REDUCE_EXPENSES", "INCREASE_REVENUE");
        adjTypeCombo.setValue("DELAY_RECEIVABLES");

        grid.add(new Label("Scenario Title:"), 0, 0);
        grid.add(nameField, 1, 0);

        grid.add(new Label("Hypothetical Shock:"), 0, 1);
        grid.add(adjTypeCombo, 1, 1);

        grid.add(new Label("Shock Amount (₹):"), 2, 0);
        grid.add(amountField, 3, 0);

        grid.add(new Label("Delay Impact (Days):"), 2, 1);
        grid.add(delayField, 3, 1);

        runButton.getStyleClass().add("button-primary");
        runButton.setOnAction(e -> executeSimulation());

        HBox btnBox = new HBox(runButton);
        btnBox.setAlignment(Pos.CENTER_LEFT);
        grid.add(btnBox, 1, 2);

        resultSummary.setStyle("-fx-text-fill: -color-accent; -fx-font-weight: bold;");

        container.getChildren().addAll(headerBox, grid, resultSummary, compChart);
        setContent(container);
    }

    @SuppressWarnings("unchecked")
    private void executeSimulation() {
        compChart.getData().clear();

        BigDecimal amount;
        int delay;
        try {
            amount = new BigDecimal(amountField.getText().trim());
            delay = Integer.parseInt(delayField.getText().trim());
        } catch (Exception ex) {
            resultSummary.setText("Invalid amount or delay number.");
            return;
        }

        Map<String, Object> sim = uiService.runScenarioSimulation(
                nameField.getText(), adjTypeCombo.getValue(), amount, delay);

        List<ForecastEntry> base = (List<ForecastEntry>) sim.get("baseline");
        List<ForecastEntry> scen = (List<ForecastEntry>) sim.get("scenario");

        if (base == null || scen == null || base.isEmpty()) {
            resultSummary.setText("Unable to run simulation: no active dataset available.");
            return;
        }

        XYChart.Series<String, Number> s1 = new XYChart.Series<>();
        s1.setName("Baseline Projection");

        XYChart.Series<String, Number> s2 = new XYChart.Series<>();
        s2.setName("Simulated Trajectory (" + nameField.getText() + ")");

        for (int i = 0; i < base.size(); i++) {
            if (i % 2 != 0 && i != base.size() - 1) continue;
            String dateLabel = base.get(i).getForecastDate().toString().substring(5);
            s1.getData().add(new XYChart.Data<>(dateLabel, base.get(i).getClosingCash().doubleValue()));
            s2.getData().add(new XYChart.Data<>(dateLabel, scen.get(i).getClosingCash().doubleValue()));
        }

        compChart.getData().addAll(s1, s2);
        resultSummary.setText("Simulation completed! Impact visualized against baseline 30-day forecast.");
    }
}
