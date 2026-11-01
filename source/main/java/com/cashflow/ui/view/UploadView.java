package com.cashflow.ui.view;

import com.cashflow.model.dto.CsvImportResult;
import com.cashflow.ui.service.CashFlowUiService;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;

import java.io.File;

public class UploadView extends VBox {

    private final CashFlowUiService uiService;
    private final Runnable onUploadSuccess;
    private final Label selectedFileLabel = new Label("No file selected.");
    private final TextArea logArea = new TextArea();
    private File chosenFile;

    public UploadView(CashFlowUiService uiService, Runnable onUploadSuccess) {
        this.uiService = uiService;
        this.onUploadSuccess = onUploadSuccess;
        setSpacing(20);
        setPadding(new Insets(24));
        buildUI();
    }

    private void buildUI() {
        VBox headerBox = new VBox(4);
        Label title = new Label("Upload Financial CSV");
        title.getStyleClass().add("header-title");
        Label subtitle = new Label("Import periodic accounting extracts. Triggers auto-versioning, change detection, and early warning recalculations.");
        subtitle.getStyleClass().add("header-subtitle");
        headerBox.getChildren().addAll(title, subtitle);

        VBox dropBox = new VBox(14);
        dropBox.getStyleClass().add("kpi-card");
        dropBox.setAlignment(Pos.CENTER);
        dropBox.setPadding(new Insets(30));

        Label dropText = new Label("Select standard CSV file with headers: transaction_id, date, type, category, description, amount, customer, customer_phone, customer_email, supplier, due_date, status");
        dropText.setStyle("-fx-text-fill: -color-text-secondary; -fx-font-size: 13px;");

        HBox btnRow = new HBox(12);
        btnRow.setAlignment(Pos.CENTER);

        Button chooseBtn = new Button("Browse CSV...");
        chooseBtn.getStyleClass().add("button-secondary");
        chooseBtn.setOnAction(e -> {
            FileChooser chooser = new FileChooser();
            chooser.setTitle("Select Transaction CSV");
            chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV Files (*.csv)", "*.csv"));
            File f = chooser.showOpenDialog(getScene().getWindow());
            if (f != null) {
                chosenFile = f;
                selectedFileLabel.setText("Selected: " + f.getName() + " (" + f.length() + " bytes)");
            }
        });

        Button uploadBtn = new Button("Process & Ingest CSV");
        uploadBtn.getStyleClass().add("button-primary");
        uploadBtn.setOnAction(e -> processUpload());

        btnRow.getChildren().addAll(chooseBtn, uploadBtn);
        dropBox.getChildren().addAll(dropText, selectedFileLabel, btnRow);

        Label logTitle = new Label("Import & Validation Log:");
        logTitle.setStyle("-fx-font-weight: bold; -fx-text-fill: -color-text-primary;");

        logArea.setEditable(false);
        logArea.setPrefHeight(240);
        logArea.setStyle("-fx-font-family: monospace;");

        getChildren().addAll(headerBox, dropBox, logTitle, logArea);
    }

    private void processUpload() {
        if (chosenFile == null) {
            logArea.setText("Error: Please select a CSV file first.");
            return;
        }

        try {
            logArea.setText("Ingesting " + chosenFile.getName() + "...\n");
            CsvImportResult res = uiService.importCsv(chosenFile);

            StringBuilder sb = new StringBuilder();
            sb.append(">>> INGESTION FINISHED <<<\n");
            sb.append("Dataset Version: V").append(res.getVersionNumber()).append("\n");
            sb.append("Total Rows Processed: ").append(res.getTotalRows()).append("\n");
            sb.append("Valid Ingested: ").append(res.getValidRows()).append("\n");
            sb.append("Invalid/Rejected: ").append(res.getInvalidRows()).append("\n\n");
            sb.append("--- DELTA CHANGE ENGINE ---\n");
            sb.append("Added Transactions:    ").append(res.getAdded()).append("\n");
            sb.append("Modified Transactions: ").append(res.getModified()).append("\n");
            sb.append("Removed Transactions:  ").append(res.getRemoved()).append("\n");
            sb.append("Unchanged Records:     ").append(res.getUnchanged()).append("\n\n");

            if (res.getErrors() != null && !res.getErrors().isEmpty()) {
                sb.append("Validation Issues:\n");
                for (String err : res.getErrors()) {
                    sb.append(" • ").append(err).append("\n");
                }
            } else {
                sb.append("All rows passed schema and domain validations.\n");
                sb.append("Forecast, anomalies, and warning engine recalculated successfully.\n");
            }

            logArea.setText(sb.toString());
            if (onUploadSuccess != null) onUploadSuccess.run();

        } catch (Exception ex) {
            logArea.setText("Upload Failed: " + ex.getMessage());
        }
    }
}
