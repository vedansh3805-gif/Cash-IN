package com.cashflow.ui.view;

import com.cashflow.ui.service.CashFlowUiService;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.chart.PieChart;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ExpensesView extends VBox {

    private final CashFlowUiService uiService;
    private final TableView<Map<String, Object>> recurringTable = new TableView<>();
    private final PieChart categoryChart = new PieChart();
    private final Label totalExpenseLabel = new Label();
    private final NumberFormat currencyFormatter = NumberFormat.getCurrencyInstance(new Locale("en", "IN"));

    public ExpensesView(CashFlowUiService uiService) {
        this.uiService = uiService;
        setSpacing(20);
        setPadding(new Insets(24));
        buildUI();
        loadData();
    }

    private void buildUI() {
        VBox headerBox = new VBox(4);
        Label title = new Label("Expense Structure & Recurring Commitments");
        title.getStyleClass().add("header-title");
        Label subtitle = new Label("Categorized spending breakdown and recurring fixed expense schedules (rent, payroll, subscriptions).");
        subtitle.getStyleClass().add("header-subtitle");
        headerBox.getChildren().addAll(title, subtitle);

        // Top Split: PieChart + Actions
        HBox topBox = new HBox(24);
        categoryChart.setTitle("Spending by Category");
        categoryChart.setPrefHeight(260);
        HBox.setHgrow(categoryChart, Priority.ALWAYS);

        VBox rightSummary = new VBox(14);
        rightSummary.setPrefWidth(300);
        rightSummary.getStyleClass().add("kpi-card");

        Label sumTitle = new Label("Total Realized Expenses");
        sumTitle.getStyleClass().add("kpi-title");
        totalExpenseLabel.getStyleClass().add("kpi-value");

        Label recInfo = new Label("Recurring payments automatically inject into future cash projections on their due dates.");
        recInfo.setWrapText(true);
        recInfo.setStyle("-fx-text-fill: -color-text-secondary; -fx-font-size: 11px;");

        rightSummary.getChildren().addAll(sumTitle, totalExpenseLabel, recInfo);
        topBox.getChildren().addAll(categoryChart, rightSummary);

        // Section 2: Recurring Table
        HBox recHeader = new HBox(12);
        recHeader.setAlignment(Pos.CENTER_LEFT);
        Label recTitle = new Label("Scheduled Recurring Payments");
        recTitle.setStyle("-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: -color-text-primary;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button addRecBtn = new Button("＋ Add Recurring Obligation");
        addRecBtn.getStyleClass().add("button-primary");
        addRecBtn.setOnAction(e -> showAddRecurringDialog());

        recHeader.getChildren().addAll(recTitle, spacer, addRecBtn);

        setupRecurringTable();
        recurringTable.setPrefHeight(220);
        VBox.setVgrow(recurringTable, Priority.ALWAYS);

        getChildren().addAll(headerBox, topBox, recHeader, recurringTable);
    }

    private void setupRecurringTable() {
        recurringTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<Map<String, Object>, String> descCol = new TableColumn<>("Obligation Description");
        descCol.setCellValueFactory(c -> new SimpleStringProperty(String.valueOf(c.getValue().get("description"))));

        TableColumn<Map<String, Object>, String> catCol = new TableColumn<>("Category");
        catCol.setCellValueFactory(c -> new SimpleStringProperty(String.valueOf(c.getValue().get("category"))));

        TableColumn<Map<String, Object>, String> amtCol = new TableColumn<>("Scheduled Amount");
        amtCol.setCellValueFactory(c -> new SimpleStringProperty("₹" + c.getValue().get("amount")));
        amtCol.setStyle("-fx-alignment: CENTER-RIGHT;");

        TableColumn<Map<String, Object>, String> freqCol = new TableColumn<>("Frequency");
        freqCol.setCellValueFactory(c -> new SimpleStringProperty(String.valueOf(c.getValue().get("frequency"))));

        TableColumn<Map<String, Object>, String> dueCol = new TableColumn<>("Next Due Date");
        dueCol.setCellValueFactory(c -> new SimpleStringProperty(String.valueOf(c.getValue().get("nextDueDate"))));

        TableColumn<Map<String, Object>, Void> actionCol = new TableColumn<>("Action");
        actionCol.setPrefWidth(90);
        actionCol.setCellFactory(param -> new TableCell<>() {
            private final Button deleteBtn = new Button("🗑 Delete");
            {
                deleteBtn.setStyle("-fx-font-size: 11px; -fx-padding: 3 8 3 8; -fx-text-fill: -color-danger;");
                deleteBtn.setOnAction(e -> {
                    Map<String, Object> item = getTableView().getItems().get(getIndex());
                    String id = String.valueOf(item.get("recurringId"));
                    try {
                        uiService.deleteRecurringPayment(id);
                        loadData();
                    } catch (Exception ex) {
                        Alert alert = new Alert(Alert.AlertType.ERROR, "Failed to delete: " + ex.getMessage(), ButtonType.OK);
                        alert.showAndWait();
                    }
                });
            }
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : deleteBtn);
            }
        });

        recurringTable.getColumns().addAll(descCol, catCol, amtCol, freqCol, dueCol, actionCol);
    }

    private void showAddRecurringDialog() {
        Stage dialog = new Stage();
        dialog.initModality(javafx.stage.Modality.APPLICATION_MODAL);
        dialog.setTitle("Add Scheduled Recurring Obligation");

        VBox root = new VBox(14);
        root.setPadding(new Insets(20));
        root.setPrefWidth(420);

        Label header = new Label("New Recurring Fixed Obligation");
        header.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);

        TextField descField = new TextField();
        descField.setPromptText("e.g. Office Rent, Cloud Hosting");

        ComboBox<String> catBox = new ComboBox<>();
        catBox.getItems().addAll("Rent", "Salary", "Utilities", "Inventory", "Marketing", "Software", "Insurance", "Taxes", "Other");
        catBox.setValue("Rent");

        TextField amtField = new TextField();
        amtField.setPromptText("e.g. 35000");

        ComboBox<String> freqBox = new ComboBox<>();
        freqBox.getItems().addAll("MONTHLY", "WEEKLY", "QUARTERLY", "ANNUALLY");
        freqBox.setValue("MONTHLY");

        DatePicker datePicker = new DatePicker(java.time.LocalDate.now().plusMonths(1).withDayOfMonth(5));

        grid.add(new Label("Description:"), 0, 0);
        grid.add(descField, 1, 0);
        grid.add(new Label("Category:"), 0, 1);
        grid.add(catBox, 1, 1);
        grid.add(new Label("Amount (₹):"), 0, 2);
        grid.add(amtField, 1, 2);
        grid.add(new Label("Frequency:"), 0, 3);
        grid.add(freqBox, 1, 3);
        grid.add(new Label("Next Due Date:"), 0, 4);
        grid.add(datePicker, 1, 4);

        Label errLabel = new Label();
        errLabel.setStyle("-fx-text-fill: -color-danger; -fx-font-size: 11px;");

        Button saveBtn = new Button("Save Obligation");
        saveBtn.getStyleClass().add("button-primary");
        saveBtn.setOnAction(e -> {
            String desc = descField.getText().trim();
            if (desc.isEmpty()) {
                errLabel.setText("Please enter an obligation description.");
                return;
            }
            BigDecimal amt;
            try {
                amt = new BigDecimal(amtField.getText().trim());
                if (amt.compareTo(BigDecimal.ZERO) <= 0) throw new IllegalArgumentException();
            } catch (Exception ex) {
                errLabel.setText("Please enter a valid positive amount.");
                return;
            }
            if (datePicker.getValue() == null) {
                errLabel.setText("Please select a due date.");
                return;
            }

            try {
                uiService.addRecurringPayment(desc, catBox.getValue(), amt, freqBox.getValue(), datePicker.getValue().toString());
                loadData();
                dialog.close();
            } catch (Exception ex) {
                errLabel.setText("Error saving: " + ex.getMessage());
            }
        });

        Button cancelBtn = new Button("Cancel");
        cancelBtn.getStyleClass().add("button-secondary");
        cancelBtn.setOnAction(e -> dialog.close());

        HBox btnBox = new HBox(10, saveBtn, cancelBtn);
        btnBox.setAlignment(Pos.CENTER_RIGHT);

        root.getChildren().addAll(header, grid, errLabel, btnBox);

        Scene scene = new Scene(root);
        if (getScene() != null && getScene().getStylesheets() != null) {
            scene.getStylesheets().addAll(getScene().getStylesheets());
        }
        dialog.setScene(scene);
        dialog.showAndWait();
    }

    @SuppressWarnings("unchecked")
    public void loadData() {
        categoryChart.getData().clear();
        Map<String, Object> data = uiService.getExpenseAnalysis();

        BigDecimal total = (BigDecimal) data.getOrDefault("totalExpense", BigDecimal.ZERO);
        totalExpenseLabel.setText(currencyFormatter.format(total));

        Map<String, BigDecimal> byCat = (Map<String, BigDecimal>) data.getOrDefault("byCategory", Map.of());
        byCat.forEach((cat, amt) -> {
            categoryChart.getData().add(new PieChart.Data(cat + " (" + currencyFormatter.format(amt) + ")", amt.doubleValue()));
        });

        List<Map<String, Object>> recurrings = uiService.getRecurringPayments();
        recurringTable.setItems(FXCollections.observableArrayList(recurrings));
    }
}
