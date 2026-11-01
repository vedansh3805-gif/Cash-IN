package com.cashflow.ui.view;

import com.cashflow.model.Warning;
import com.cashflow.ui.service.CashFlowUiService;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public class WarningsView extends VBox {

    private final CashFlowUiService uiService;
    private final TableView<Warning> warningsTable = new TableView<>();
    private final TableView<Map<String, Object>> anomaliesTable = new TableView<>();

    public WarningsView(CashFlowUiService uiService) {
        this.uiService = uiService;
        setSpacing(20);
        setPadding(new Insets(24));
        buildUI();
        loadData();
    }

    private void buildUI() {
        VBox headerBox = new VBox(4);
        Label title = new Label("Early Warning System & Anomaly Radar");
        title.getStyleClass().add("header-title");
        Label subtitle = new Label("Explainable alerts flagging projected cash deficits, overdue collections, and statistical spending outliers.");
        subtitle.getStyleClass().add("header-subtitle");
        headerBox.getChildren().addAll(title, subtitle);

        // Section 1: Warnings
        Label warningsTitle = new Label("Active Financial Warnings");
        warningsTitle.setStyle("-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: -color-text-primary;");

        setupWarningsTable();
        warningsTable.setPrefHeight(220);
        VBox.setVgrow(warningsTable, Priority.ALWAYS);

        // Section 2: Anomalies
        Label anomaliesTitle = new Label("Detected Financial Anomalies");
        anomaliesTitle.setStyle("-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: -color-text-primary;");

        setupAnomaliesTable();
        anomaliesTable.setPrefHeight(220);
        VBox.setVgrow(anomaliesTable, Priority.ALWAYS);

        getChildren().addAll(headerBox, warningsTitle, warningsTable, anomaliesTitle, anomaliesTable);
    }

    private void setupWarningsTable() {
        warningsTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<Warning, String> sevCol = new TableColumn<>("Severity");
        sevCol.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getSeverity()));
        sevCol.setPrefWidth(100);

        TableColumn<Warning, String> typeCol = new TableColumn<>("Warning Type");
        typeCol.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getWarningType()));
        typeCol.setPrefWidth(160);

        TableColumn<Warning, LocalDate> projCol = new TableColumn<>("Projected Date");
        projCol.setCellValueFactory(c -> new SimpleObjectProperty<>(c.getValue().getProjectedDate()));
        projCol.setPrefWidth(120);

        TableColumn<Warning, String> msgCol = new TableColumn<>("Explanation / Root Cause");
        msgCol.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getMessage()));
        msgCol.setPrefWidth(400);

        TableColumn<Warning, Void> actCol = new TableColumn<>("Outreach Action");
        actCol.setPrefWidth(150);
        actCol.setCellFactory(param -> new TableCell<>() {
            private final Button notifyBtn = new Button("⚡ Notify Parties");
            {
                notifyBtn.setStyle("-fx-font-size: 11px; -fx-padding: 3 8 3 8;");
                notifyBtn.setOnAction(e -> {
                    Warning w = getTableView().getItems().get(getIndex());
                    handleWarningAction(w);
                });
            }
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    Warning w = getTableView().getItems().get(getIndex());
                    if ("OVERDUE_RECEIVABLE".equalsIgnoreCase(w.getWarningType())) {
                        notifyBtn.getStyleClass().add("button-primary");
                        setGraphic(notifyBtn);
                    } else {
                        setGraphic(null);
                    }
                }
            }
        });

        warningsTable.getColumns().addAll(sevCol, typeCol, projCol, msgCol, actCol);
    }

    private void handleWarningAction(Warning w) {
        try {
            Map<String, Object> recData = uiService.getReceivablesAnalysis();
            @SuppressWarnings("unchecked")
            List<com.cashflow.model.Transaction> recs = (List<com.cashflow.model.Transaction>) recData.getOrDefault("receivables", List.of());
            int count = 0;
            for (com.cashflow.model.Transaction t : recs) {
                if ("OVERDUE".equalsIgnoreCase(t.getStatus())) {
                    uiService.notifyBothParties(t.getTransactionId());
                    count++;
                }
            }
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Defaulter Outreach Triggered");
            alert.setHeaderText("Multi-Channel Notices Dispatched");
            alert.setContentText("Dispatched SMS reminders and email notices to " + count + " overdue debtor(s) and audit copies to shopkeeper.");
            alert.showAndWait();
        } catch (Exception ex) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Action Failed");
            alert.setHeaderText("Error dispatching alerts");
            alert.setContentText(ex.getMessage());
            alert.showAndWait();
        }
    }

    private void setupAnomaliesTable() {
        anomaliesTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<Map<String, Object>, String> txnCol = new TableColumn<>("TXN ID");
        txnCol.setCellValueFactory(c -> new SimpleStringProperty(String.valueOf(c.getValue().get("transactionId"))));
        txnCol.setPrefWidth(100);

        TableColumn<Map<String, Object>, String> typeCol = new TableColumn<>("Type");
        typeCol.setCellValueFactory(c -> new SimpleStringProperty(String.valueOf(c.getValue().get("type"))));
        typeCol.setPrefWidth(140);

        TableColumn<Map<String, Object>, String> actCol = new TableColumn<>("Actual Amount");
        actCol.setCellValueFactory(c -> new SimpleStringProperty("₹" + c.getValue().get("actualValue")));
        actCol.setPrefWidth(120);

        TableColumn<Map<String, Object>, String> expCol = new TableColumn<>("Expected Value");
        expCol.setCellValueFactory(c -> new SimpleStringProperty("₹" + c.getValue().get("expectedValue")));
        expCol.setPrefWidth(120);

        TableColumn<Map<String, Object>, String> explCol = new TableColumn<>("Analytical Reason");
        explCol.setCellValueFactory(c -> new SimpleStringProperty(String.valueOf(c.getValue().get("explanation"))));
        explCol.setPrefWidth(400);

        anomaliesTable.getColumns().addAll(txnCol, typeCol, actCol, expCol, explCol);
    }

    public void loadData() {
        List<Warning> warnings = uiService.getWarnings();
        warningsTable.setItems(FXCollections.observableArrayList(warnings));

        List<Map<String, Object>> anomalies = uiService.getAnomalies();
        anomaliesTable.setItems(FXCollections.observableArrayList(anomalies));
    }
}
