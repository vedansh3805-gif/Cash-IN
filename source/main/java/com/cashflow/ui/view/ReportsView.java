package com.cashflow.ui.view;

import com.cashflow.ui.service.CashFlowUiService;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;

import java.io.File;
import java.util.List;
import java.util.Map;

public class ReportsView extends VBox {

    private final CashFlowUiService uiService;
    private final Label statusLabel = new Label();

    public ReportsView(CashFlowUiService uiService) {
        this.uiService = uiService;
        setSpacing(20);
        setPadding(new Insets(24));
        buildUI();
    }

    private void buildUI() {
        VBox headerBox = new VBox(4);
        Label title = new Label("Financial Reports & CSV Export");
        title.getStyleClass().add("header-title");
        Label subtitle = new Label("Generate auditable balance statements and export clean CSV ledgers for accounting teams.");
        subtitle.getStyleClass().add("header-subtitle");
        headerBox.getChildren().addAll(title, subtitle);

        HBox cardsBox = new HBox(20);

        // Card 1: CSV Export
        VBox csvCard = new VBox(14);
        csvCard.getStyleClass().add("kpi-card");
        csvCard.setPrefWidth(380);

        Label csvTitle = new Label("Export Consolidated Dataset (CSV)");
        csvTitle.setStyle("-fx-font-weight: bold; -fx-text-fill: -color-text-primary;");

        Label csvDesc = new Label("Download all validated transactions in the current active version into an exportable standard CSV ledger.");
        csvDesc.setWrapText(true);
        csvDesc.setStyle("-fx-text-fill: -color-text-secondary; -fx-font-size: 12px;");

        Button exportBtn = new Button("Export Transactions CSV");
        exportBtn.getStyleClass().add("button-primary");
        exportBtn.setOnAction(e -> doExport());

        statusLabel.setStyle("-fx-text-fill: -color-accent; -fx-font-weight: bold; -fx-font-size: 11px;");

        csvCard.getChildren().addAll(csvTitle, csvDesc, exportBtn, statusLabel);

        // Card 2: Executive Summary Actions
        VBox execCard = new VBox(14);
        execCard.getStyleClass().add("kpi-card");
        execCard.setPrefWidth(380);

        Label execTitle = new Label("Executive Audit Statement");
        execTitle.setStyle("-fx-font-weight: bold; -fx-text-fill: -color-text-primary;");

        Label execDesc = new Label("Generate an executive plain-text financial position statement for accountants, partners, and loan applications.");
        execDesc.setWrapText(true);
        execDesc.setStyle("-fx-text-fill: -color-text-secondary; -fx-font-size: 12px;");

        HBox execBtnRow = new HBox(10);
        Button genBtn = new Button("📄 Refresh Statement");
        genBtn.getStyleClass().add("button-secondary");
        genBtn.setOnAction(e -> generateStatementPreview());

        Button copyBtn = new Button("📋 Copy to Clipboard");
        copyBtn.getStyleClass().add("button-secondary");
        copyBtn.setOnAction(e -> copyStatementToClipboard());

        Button saveTxtBtn = new Button("💾 Save Text Report");
        saveTxtBtn.getStyleClass().add("button-primary");
        saveTxtBtn.setOnAction(e -> saveStatementReport());

        execBtnRow.getChildren().addAll(genBtn, copyBtn, saveTxtBtn);
        execCard.getChildren().addAll(execTitle, execDesc, execBtnRow);

        cardsBox.getChildren().addAll(csvCard, execCard);

        // Section 3: Live Preview
        Label previewTitle = new Label("Executive Statement Preview:");
        previewTitle.setStyle("-fx-font-weight: bold; -fx-text-fill: -color-text-primary; -fx-font-size: 13px;");

        statementArea.setEditable(false);
        statementArea.setPrefHeight(280);
        statementArea.setStyle("-fx-font-family: monospace; -fx-font-size: 12px;");
        VBox.setVgrow(statementArea, javafx.scene.layout.Priority.ALWAYS);

        generateStatementPreview();

        getChildren().addAll(headerBox, cardsBox, previewTitle, statementArea);
    }

    private final javafx.scene.control.TextArea statementArea = new javafx.scene.control.TextArea();

    private void generateStatementPreview() {
        try {
            com.cashflow.model.dto.DashboardMetrics m = uiService.getDashboardMetrics();
            Map<String, Object> rec = uiService.getReceivablesAnalysis();
            Map<String, Object> exp = uiService.getExpenseAnalysis();
            List<com.cashflow.model.Warning> warnings = uiService.getWarnings();

            StringBuilder sb = new StringBuilder();
            sb.append("================================================================================\n");
            sb.append("                    CASH-IN FINANCIAL POSITION STATEMENT                        \n");
            sb.append("                    Generated: ").append(java.time.LocalDateTime.now().toString().replace("T", " ")).append("\n");
            sb.append("================================================================================\n\n");

            sb.append("1. CORE LIQUIDITY METRICS\n");
            sb.append("   - Realized Bank Cash:        ₹").append(m != null && m.getCurrentCash() != null ? m.getCurrentCash() : "0.00").append("\n");
            sb.append("   - Expected Incoming (30d):   ₹").append(m != null && m.getExpectedIncoming() != null ? m.getExpectedIncoming() : "0.00").append("\n");
            sb.append("   - Upcoming Expenses (30d):   ₹").append(m != null && m.getUpcomingExpenses() != null ? m.getUpcomingExpenses() : "0.00").append("\n");
            sb.append("   - Projected Net Cash Flow:   ₹").append(m != null && m.getNetCashFlow() != null ? m.getNetCashFlow() : "0.00").append("\n");
            sb.append("   - 90-Day Cash Trajectory Floor: ₹").append(m != null && m.getForecastedMinCash() != null ? m.getForecastedMinCash() : "0.00").append("\n");
            sb.append("   - Early Warning Risk Status: ").append(m != null && m.getRiskStatus() != null ? m.getRiskStatus() : "LOW").append("\n\n");

            sb.append("2. RECEIVABLES & DEBTORS\n");
            sb.append("   - Total Outstanding Invoices: ₹").append(rec.getOrDefault("totalOutstanding", "0.00")).append("\n");
            sb.append("   - Overdue Invoices (Late):    ₹").append(rec.getOrDefault("overdueAmount", "0.00")).append("\n\n");

            sb.append("3. COMMITTED EXPENSES\n");
            sb.append("   - Total Realized Expenses:    ₹").append(exp.getOrDefault("totalExpense", "0.00")).append("\n\n");

            sb.append("4. EARLY WARNING SIGNALS (").append(warnings.size()).append(")\n");
            if (warnings.isEmpty()) {
                sb.append("   • No liquidity breaches or overdue risk signals detected.\n");
            } else {
                for (com.cashflow.model.Warning w : warnings) {
                    sb.append("   • [").append(w.getSeverity()).append("] ").append(w.getMessage()).append("\n");
                }
            }
            sb.append("\n================================================================================\n");
            statementArea.setText(sb.toString());
        } catch (Exception ex) {
            statementArea.setText("Unable to generate preview: " + ex.getMessage());
        }
    }

    private void copyStatementToClipboard() {
        javafx.scene.input.Clipboard clipboard = javafx.scene.input.Clipboard.getSystemClipboard();
        javafx.scene.input.ClipboardContent content = new javafx.scene.input.ClipboardContent();
        content.putString(statementArea.getText());
        clipboard.setContent(content);
        statusLabel.setText("✓ Statement copied to clipboard!");
    }

    private void saveStatementReport() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Save Statement Report");
        chooser.setInitialFileName("cashflow_statement.txt");
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Text Files (*.txt)", "*.txt"));
        File dest = chooser.showSaveDialog(getScene().getWindow());
        if (dest != null) {
            try (java.io.FileWriter w = new java.io.FileWriter(dest)) {
                w.write(statementArea.getText());
                statusLabel.setText("✓ Statement saved to: " + dest.getName());
            } catch (Exception ex) {
                statusLabel.setText("Failed to save statement: " + ex.getMessage());
            }
        }
    }

    private void doExport() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Save Exported CSV");
        chooser.setInitialFileName("cashflow_export.csv");
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV Files (*.csv)", "*.csv"));
        File dest = chooser.showSaveDialog(getScene().getWindow());

        if (dest != null) {
            try {
                uiService.exportReportCsv(dest);
                statusLabel.setText("Exported successfully to: " + dest.getAbsolutePath());
            } catch (Exception ex) {
                statusLabel.setText("Export failed: " + ex.getMessage());
            }
        }
    }
}
