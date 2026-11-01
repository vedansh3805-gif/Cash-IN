package com.cashflow.ui.view;

import com.cashflow.model.Transaction;
import com.cashflow.ui.service.CashFlowUiService;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

public class ReceivablesView extends VBox {

    private final CashFlowUiService uiService;
    private final TableView<Transaction> table = new TableView<>();
    private final HBox kpiBox = new HBox(16);
    private final NumberFormat currencyFormatter = NumberFormat.getCurrencyInstance(new Locale("en", "IN"));
    private final ObservableList<Transaction> masterData = FXCollections.observableArrayList();
    private String currentFilter = "ALL";

    public ReceivablesView(CashFlowUiService uiService) {
        this.uiService = uiService;
        setSpacing(20);
        setPadding(new Insets(24));
        buildUI();
        loadData();
    }

    private void buildUI() {
        VBox headerBox = new VBox(4);
        Label title = new Label("Receivables, Defaulters & Multi-Channel Outreach");
        title.getStyleClass().add("header-title");
        Label subtitle = new Label("Track delayed payments and defaulters. Initiate phone calls, dispatch reminder SMS & formal emails to both Buyer and Shopkeeper.");
        subtitle.getStyleClass().add("header-subtitle");
        headerBox.getChildren().addAll(title, subtitle);

        // Filter & Action Toolbar
        HBox toolbar = new HBox(12);
        toolbar.setAlignment(Pos.CENTER_LEFT);

        ComboBox<String> filterCombo = new ComboBox<>();
        filterCombo.getItems().addAll("ALL INVOICES", "OVERDUE DEFAULTERS ONLY", "PENDING ONLY");
        filterCombo.setValue("ALL INVOICES");
        filterCombo.setOnAction(e -> {
            String val = filterCombo.getValue();
            if ("OVERDUE DEFAULTERS ONLY".equals(val)) {
                currentFilter = "OVERDUE";
            } else if ("PENDING ONLY".equals(val)) {
                currentFilter = "PENDING";
            } else {
                currentFilter = "ALL";
            }
            applyFilter();
        });

        Button bulkAlertBtn = new Button("⚡ Notify All Defaulters (Dual-Party SMS & Email)");
        bulkAlertBtn.getStyleClass().add("button-primary");
        bulkAlertBtn.setOnAction(e -> notifyAllDefaulters());

        Button commLogBtn = new Button("📜 Communication & Call History");
        commLogBtn.getStyleClass().add("button-secondary");
        commLogBtn.setOnAction(e -> showCommunicationHistoryModal());

        Button refreshBtn = new Button("Refresh");
        refreshBtn.getStyleClass().add("button-secondary");
        refreshBtn.setOnAction(e -> loadData());

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        toolbar.getChildren().addAll(
                new Label("Filter View:"), filterCombo,
                spacer,
                bulkAlertBtn,
                commLogBtn,
                refreshBtn
        );

        setupTable();
        VBox.setVgrow(table, Priority.ALWAYS);

        getChildren().addAll(headerBox, kpiBox, toolbar, table);
    }

    private void setupTable() {
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<Transaction, String> idCol = new TableColumn<>("Invoice ID");
        idCol.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getTransactionId()));
        idCol.setPrefWidth(90);

        TableColumn<Transaction, String> custCol = new TableColumn<>("Buyer / Customer");
        custCol.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getCustomer() != null ? c.getValue().getCustomer() : "—"));
        custCol.setPrefWidth(140);

        TableColumn<Transaction, String> phoneCol = new TableColumn<>("Buyer Phone");
        phoneCol.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getCustomerPhone() != null && !c.getValue().getCustomerPhone().isBlank() ? c.getValue().getCustomerPhone() : "—"));
        phoneCol.setPrefWidth(125);

        TableColumn<Transaction, String> emailCol = new TableColumn<>("Buyer Email");
        emailCol.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getCustomerEmail() != null && !c.getValue().getCustomerEmail().isBlank() ? c.getValue().getCustomerEmail() : "—"));
        emailCol.setPrefWidth(160);

        TableColumn<Transaction, LocalDate> dueCol = new TableColumn<>("Due Date");
        dueCol.setCellValueFactory(c -> new SimpleObjectProperty<>(c.getValue().getDueDate()));
        dueCol.setPrefWidth(95);

        TableColumn<Transaction, String> amtCol = new TableColumn<>("Outstanding");
        amtCol.setCellValueFactory(c -> new SimpleStringProperty(formatAmount(c.getValue().getAmount())));
        amtCol.setStyle("-fx-alignment: CENTER-RIGHT;");
        amtCol.setPrefWidth(110);

        TableColumn<Transaction, String> statusCol = new TableColumn<>("Status");
        statusCol.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getStatus()));
        statusCol.setPrefWidth(85);

        TableColumn<Transaction, Void> actionCol = new TableColumn<>("Actions (Buyer & Shopkeeper)");
        actionCol.setPrefWidth(280);
        actionCol.setCellFactory(param -> new TableCell<>() {
            private final Button callBtn = new Button("📞 Call");
            private final Button smsBtn = new Button("💬 SMS");
            private final Button emailBtn = new Button("✉️ Email");
            private final Button alertBothBtn = new Button("⚡ Both");
            private final HBox btnBox = new HBox(6, callBtn, smsBtn, emailBtn, alertBothBtn);

            {
                callBtn.setStyle("-fx-font-size: 11px; -fx-padding: 3 7 3 7;");
                smsBtn.setStyle("-fx-font-size: 11px; -fx-padding: 3 7 3 7;");
                emailBtn.setStyle("-fx-font-size: 11px; -fx-padding: 3 7 3 7;");
                alertBothBtn.setStyle("-fx-font-size: 11px; -fx-font-weight: bold; -fx-padding: 3 7 3 7;");

                callBtn.setOnAction(e -> {
                    Transaction t = getTableView().getItems().get(getIndex());
                    showCallDialog(t);
                });

                smsBtn.setOnAction(e -> {
                    Transaction t = getTableView().getItems().get(getIndex());
                    showSmsDialog(t);
                });

                emailBtn.setOnAction(e -> {
                    Transaction t = getTableView().getItems().get(getIndex());
                    showEmailDialog(t);
                });

                alertBothBtn.setOnAction(e -> {
                    Transaction t = getTableView().getItems().get(getIndex());
                    notifySingleTransactionBothParties(t);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    Transaction t = getTableView().getItems().get(getIndex());
                    if ("OVERDUE".equalsIgnoreCase(t.getStatus())) {
                        alertBothBtn.setStyle("-fx-font-size: 11px; -fx-font-weight: bold; -fx-padding: 3 7 3 7; -fx-background-color: -color-danger; -fx-text-fill: white;");
                    } else {
                        alertBothBtn.setStyle("-fx-font-size: 11px; -fx-padding: 3 7 3 7;");
                    }
                    setGraphic(btnBox);
                }
            }
        });

        table.getColumns().addAll(idCol, custCol, phoneCol, emailCol, dueCol, amtCol, statusCol, actionCol);
    }

    @SuppressWarnings("unchecked")
    public void loadData() {
        kpiBox.getChildren().clear();
        Map<String, Object> data = uiService.getReceivablesAnalysis();

        BigDecimal total = (BigDecimal) data.getOrDefault("totalOutstanding", BigDecimal.ZERO);
        BigDecimal overdue = (BigDecimal) data.getOrDefault("overdueAmount", BigDecimal.ZERO);
        List<Transaction> list = (List<Transaction>) data.getOrDefault("receivables", List.of());

        masterData.setAll(list);
        applyFilter();

        long overdueCount = list.stream().filter(t -> "OVERDUE".equalsIgnoreCase(t.getStatus())).count();

        kpiBox.getChildren().addAll(
                createCard("Total Receivables", formatAmount(total), false),
                createCard("Overdue (Defaulters)", formatAmount(overdue) + " (" + overdueCount + ")", overdueCount > 0),
                createCard("Active Invoices", String.valueOf(list.size()), false)
        );
    }

    private void applyFilter() {
        if ("OVERDUE".equalsIgnoreCase(currentFilter)) {
            table.setItems(masterData.filtered(t -> "OVERDUE".equalsIgnoreCase(t.getStatus())));
        } else if ("PENDING".equalsIgnoreCase(currentFilter)) {
            table.setItems(masterData.filtered(t -> "PENDING".equalsIgnoreCase(t.getStatus())));
        } else {
            table.setItems(masterData);
        }
    }

    private VBox createCard(String title, String val, boolean alert) {
        VBox b = new VBox(4);
        b.getStyleClass().add("kpi-card");
        b.setPrefWidth(220);
        Label t = new Label(title);
        t.getStyleClass().add("kpi-title");
        Label v = new Label(val);
        v.getStyleClass().add("kpi-value");
        if (alert) v.setStyle("-fx-text-fill: -color-danger;");
        b.getChildren().addAll(t, v);
        return b;
    }

    private String formatAmount(BigDecimal amount) {
        if (amount == null) return "₹0.00";
        return currencyFormatter.format(amount);
    }

    // ==========================================
    // 📞 Call Dialog
    // ==========================================
    private void showCallDialog(Transaction t) {
        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setTitle("Initiate Voice Call — Invoice #" + t.getTransactionId());

        VBox root = new VBox(14);
        root.setPadding(new Insets(20));
        root.setPrefWidth(480);

        Label header = new Label("Direct Defaulter Call");
        header.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(8);

        Map<String, Object> settings = uiService.getSettings();
        String shopkeeperPhone = String.valueOf(settings.getOrDefault("shopkeeperPhone", "+91 98765 00000"));
        String buyerPhone = t.getCustomerPhone() != null && !t.getCustomerPhone().isBlank() ? t.getCustomerPhone() : "+91 98765 10003";

        grid.add(new Label("Calling From (Shopkeeper):"), 0, 0);
        grid.add(new Label(shopkeeperPhone), 1, 0);
        grid.add(new Label("Calling To (Buyer):"), 0, 1);
        grid.add(new Label((t.getCustomer() != null ? t.getCustomer() : "Buyer") + " — " + buyerPhone), 1, 1);
        grid.add(new Label("Outstanding Amount:"), 0, 2);
        grid.add(new Label(formatAmount(t.getAmount()) + " (" + t.getStatus() + ")"), 1, 2);
        grid.add(new Label("Due Date:"), 0, 3);
        grid.add(new Label(String.valueOf(t.getDueDate())), 1, 3);

        Label notesLabel = new Label("Call Notes / Outcome:");
        TextArea notesArea = new TextArea("Discussed overdue payment. Buyer agreed to settle via bank transfer.");
        notesArea.setPrefRowCount(3);

        Label statusLbl = new Label();
        statusLbl.setStyle("-fx-font-weight: bold; -fx-text-fill: -color-accent;");

        Button dialBtn = new Button("📞 Place Call & Log Event");
        dialBtn.getStyleClass().add("button-primary");
        dialBtn.setOnAction(e -> {
            try {
                Map<String, Object> res = uiService.initiateCall(t.getTransactionId(), notesArea.getText().trim());
                statusLbl.setText("✓ " + res.getOrDefault("message", "Call connected and recorded to audit trail."));
                dialBtn.setDisable(true);
            } catch (Exception ex) {
                statusLbl.setStyle("-fx-font-weight: bold; -fx-text-fill: -color-danger;");
                statusLbl.setText("Error: " + ex.getMessage());
            }
        });

        Button closeBtn = new Button("Close");
        closeBtn.getStyleClass().add("button-secondary");
        closeBtn.setOnAction(e -> dialog.close());

        HBox btnRow = new HBox(10, dialBtn, closeBtn);
        btnRow.setAlignment(Pos.CENTER_RIGHT);

        root.getChildren().addAll(header, grid, notesLabel, notesArea, statusLbl, btnRow);

        Scene scene = new Scene(root);
        if (getScene() != null && getScene().getStylesheets() != null) {
            scene.getStylesheets().addAll(getScene().getStylesheets());
        }
        dialog.setScene(scene);
        dialog.showAndWait();
    }

    // ==========================================
    // 💬 SMS Dialog (Buyer & Shopkeeper)
    // ==========================================
    private void showSmsDialog(Transaction t) {
        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setTitle("Dispatch Dual-Party SMS — Invoice #" + t.getTransactionId());

        VBox root = new VBox(14);
        root.setPadding(new Insets(20));
        root.setPrefWidth(540);

        Label header = new Label("Send SMS Notice to Both Ends of Party");
        header.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        Map<String, Object> settings = uiService.getSettings();
        String shopkeeperPhone = String.valueOf(settings.getOrDefault("shopkeeperPhone", "+91 98765 00000"));
        String buyerPhone = t.getCustomerPhone() != null && !t.getCustomerPhone().isBlank() ? t.getCustomerPhone() : "+91 98765 10003";
        String customer = t.getCustomer() != null ? t.getCustomer() : "Buyer";

        Label buyerLabel = new Label("1. Buyer SMS Notice (to " + customer + " at " + buyerPhone + "):");
        buyerLabel.setStyle("-fx-font-weight: bold;");
        TextArea buyerMsgArea = new TextArea(
                String.format("CASH-IN ALERT: Dear %s, payment of %s for Invoice #%s is %s (Due: %s). Please remit immediately to clear balance. - CashFlow Store (%s)",
                        customer, formatAmount(t.getAmount()), t.getTransactionId(), t.getStatus(), t.getDueDate(), shopkeeperPhone)
        );
        buyerMsgArea.setPrefRowCount(3);

        Label shopkeeperLabel = new Label("2. Shopkeeper Audit SMS (to Shopkeeper at " + shopkeeperPhone + "):");
        shopkeeperLabel.setStyle("-fx-font-weight: bold;");
        TextArea shopkeeperMsgArea = new TextArea(
                String.format("SHOPKEEPER AUDIT: Overdue reminder SMS dispatched to %s (%s) for Invoice #%s (%s, Status: %s).",
                        customer, buyerPhone, t.getTransactionId(), formatAmount(t.getAmount()), t.getStatus())
        );
        shopkeeperMsgArea.setPrefRowCount(2);
        shopkeeperMsgArea.setEditable(false);

        Label statusLbl = new Label();
        statusLbl.setStyle("-fx-font-weight: bold; -fx-text-fill: -color-accent;");

        Button sendBtn = new Button("🚀 Dispatch SMS to Both Parties");
        sendBtn.getStyleClass().add("button-primary");
        sendBtn.setOnAction(e -> {
            try {
                Map<String, Object> res = uiService.sendSmsNotice(t.getTransactionId(), buyerMsgArea.getText().trim());
                statusLbl.setText("✓ " + res.getOrDefault("message", "SMS dispatched to both Buyer and Shopkeeper!"));
                sendBtn.setDisable(true);
            } catch (Exception ex) {
                statusLbl.setStyle("-fx-font-weight: bold; -fx-text-fill: -color-danger;");
                statusLbl.setText("Error: " + ex.getMessage());
            }
        });

        Button closeBtn = new Button("Close");
        closeBtn.getStyleClass().add("button-secondary");
        closeBtn.setOnAction(e -> dialog.close());

        HBox btnRow = new HBox(10, sendBtn, closeBtn);
        btnRow.setAlignment(Pos.CENTER_RIGHT);

        root.getChildren().addAll(header, buyerLabel, buyerMsgArea, shopkeeperLabel, shopkeeperMsgArea, statusLbl, btnRow);

        Scene scene = new Scene(root);
        if (getScene() != null && getScene().getStylesheets() != null) {
            scene.getStylesheets().addAll(getScene().getStylesheets());
        }
        dialog.setScene(scene);
        dialog.showAndWait();
    }

    // ==========================================
    // ✉️ Email Dialog (Buyer & Shopkeeper)
    // ==========================================
    private void showEmailDialog(Transaction t) {
        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setTitle("Send Formal Email Notice — Invoice #" + t.getTransactionId());

        VBox root = new VBox(14);
        root.setPadding(new Insets(20));
        root.setPrefWidth(600);

        Label header = new Label("Formal Overdue Email (Dispatched to Buyer & Shopkeeper)");
        header.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        Map<String, Object> settings = uiService.getSettings();
        String shopkeeperEmail = String.valueOf(settings.getOrDefault("shopkeeperEmail", "shopkeeper@cashflow.com"));
        String buyerEmail = t.getCustomerEmail() != null && !t.getCustomerEmail().isBlank() ? t.getCustomerEmail() : "finance@buyer.com";
        String customer = t.getCustomer() != null ? t.getCustomer() : "Valued Customer";

        Label subjLbl = new Label("Email Subject:");
        TextField subjectField = new TextField("URGENT: Outstanding Payment Notice for Invoice #" + t.getTransactionId() + " (" + formatAmount(t.getAmount()) + ")");

        Label bodyLbl = new Label("Buyer Email Body (Copy also routed to Shopkeeper: " + shopkeeperEmail + "):");
        TextArea bodyArea = new TextArea(
                String.format("Dear %s,\n\n" +
                                "This is a formal payment notice regarding your overdue account balance.\n\n" +
                                "• Invoice Number: %s\n" +
                                "• Outstanding Amount: %s\n" +
                                "• Due Date: %s\n" +
                                "• Status: %s\n\n" +
                                "Please clear this balance promptly via bank remittance.\n" +
                                "Shopkeeper Contact: %s | %s\n\n" +
                                "Sincerely,\nAccounts Receivable Department",
                        customer, t.getTransactionId(), formatAmount(t.getAmount()), t.getDueDate(), t.getStatus(),
                        settings.getOrDefault("shopkeeperPhone", "+91 98765 00000"), shopkeeperEmail)
        );
        bodyArea.setPrefRowCount(8);

        Label statusLbl = new Label();
        statusLbl.setStyle("-fx-font-weight: bold; -fx-text-fill: -color-accent;");

        Button sendBtn = new Button("✉️ Send Email to Both Parties");
        sendBtn.getStyleClass().add("button-primary");
        sendBtn.setOnAction(e -> {
            try {
                Map<String, Object> res = uiService.sendEmailNotice(t.getTransactionId(), subjectField.getText().trim(), bodyArea.getText().trim());
                statusLbl.setText("✓ " + res.getOrDefault("message", "Email delivered to both Buyer and Shopkeeper!"));
                sendBtn.setDisable(true);
            } catch (Exception ex) {
                statusLbl.setStyle("-fx-font-weight: bold; -fx-text-fill: -color-danger;");
                statusLbl.setText("Error: " + ex.getMessage());
            }
        });

        Button closeBtn = new Button("Close");
        closeBtn.getStyleClass().add("button-secondary");
        closeBtn.setOnAction(e -> dialog.close());

        HBox btnRow = new HBox(10, sendBtn, closeBtn);
        btnRow.setAlignment(Pos.CENTER_RIGHT);

        root.getChildren().addAll(header, subjLbl, subjectField, bodyLbl, bodyArea, statusLbl, btnRow);

        Scene scene = new Scene(root);
        if (getScene() != null && getScene().getStylesheets() != null) {
            scene.getStylesheets().addAll(getScene().getStylesheets());
        }
        dialog.setScene(scene);
        dialog.showAndWait();
    }

    // ==========================================
    // ⚡ 1-Click Dual-Party Notification
    // ==========================================
    private void notifySingleTransactionBothParties(Transaction t) {
        try {
            Map<String, Object> res = uiService.notifyBothParties(t.getTransactionId());
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Dual-Party Notice Dispatched");
            alert.setHeaderText("Outreach Dispatched for Invoice #" + t.getTransactionId());
            alert.setContentText("SMS and Email notifications successfully delivered to both:\n" +
                    "1. Buyer: " + t.getCustomer() + " (" + (t.getCustomerPhone() != null ? t.getCustomerPhone() : "Phone") + " / " + (t.getCustomerEmail() != null ? t.getCustomerEmail() : "Email") + ")\n" +
                    "2. Shopkeeper: Audit copy delivered.");
            alert.showAndWait();
        } catch (Exception ex) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Notification Failed");
            alert.setHeaderText("Failed to dispatch alert");
            alert.setContentText(ex.getMessage());
            alert.showAndWait();
        }
    }

    private void notifyAllDefaulters() {
        List<Transaction> overdueList = masterData.stream()
                .filter(t -> "OVERDUE".equalsIgnoreCase(t.getStatus()))
                .collect(Collectors.toList());

        if (overdueList.isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("No Overdue Defaulters");
            alert.setHeaderText("All payments are up to date!");
            alert.setContentText("No transactions currently marked as OVERDUE.");
            alert.showAndWait();
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirm Bulk Outreach");
        confirm.setHeaderText("Dispatch Alerts to All " + overdueList.size() + " Defaulters?");
        confirm.setContentText("This will send SMS reminders and Email notices to each buyer AND audit copies to the shopkeeper.");
        Optional<ButtonType> opt = confirm.showAndWait();

        if (opt.isPresent() && opt.get() == ButtonType.OK) {
            int count = 0;
            for (Transaction t : overdueList) {
                try {
                    uiService.notifyBothParties(t.getTransactionId());
                    count++;
                } catch (Exception ignored) {}
            }

            Alert success = new Alert(Alert.AlertType.INFORMATION);
            success.setTitle("Bulk Outreach Complete");
            success.setHeaderText("Dispatched Multi-Channel Notices");
            success.setContentText("Successfully notified " + count + " defaulters and sent audit logs to shopkeeper.");
            success.showAndWait();
        }
    }

    // ==========================================
    // 📜 Communication History Modal
    // ==========================================
    private void showCommunicationHistoryModal() {
        Stage stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setTitle("Multi-Party Communication & Call Audit Trail");

        VBox root = new VBox(14);
        root.setPadding(new Insets(20));
        root.setPrefSize(850, 500);

        Label header = new Label("Communication History & Outreach Audit Log");
        header.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
        Label sub = new Label("Complete chronological record of all phone calls, SMS notices, and emails sent to buyers and shopkeeper.");
        sub.setStyle("-fx-text-fill: -color-text-secondary;");

        TableView<Map<String, Object>> logTable = new TableView<>();
        logTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<Map<String, Object>, String> timeCol = new TableColumn<>("Timestamp");
        timeCol.setCellValueFactory(c -> new SimpleStringProperty(String.valueOf(c.getValue().getOrDefault("timestamp", ""))));
        timeCol.setPrefWidth(140);

        TableColumn<Map<String, Object>, String> txCol = new TableColumn<>("Invoice ID");
        txCol.setCellValueFactory(c -> new SimpleStringProperty(String.valueOf(c.getValue().getOrDefault("transactionId", ""))));
        txCol.setPrefWidth(85);

        TableColumn<Map<String, Object>, String> custCol = new TableColumn<>("Debtor / Buyer");
        custCol.setCellValueFactory(c -> new SimpleStringProperty(String.valueOf(c.getValue().getOrDefault("customerName", ""))));
        custCol.setPrefWidth(120);

        TableColumn<Map<String, Object>, String> chCol = new TableColumn<>("Channel");
        chCol.setCellValueFactory(c -> new SimpleStringProperty(String.valueOf(c.getValue().getOrDefault("channel", ""))));
        chCol.setPrefWidth(70);

        TableColumn<Map<String, Object>, String> recCol = new TableColumn<>("Party / Destination");
        recCol.setCellValueFactory(c -> {
            String party = String.valueOf(c.getValue().getOrDefault("recipientType", ""));
            String contact = String.valueOf(c.getValue().getOrDefault("recipientContact", ""));
            return new SimpleStringProperty(party + " (" + contact + ")");
        });
        recCol.setPrefWidth(160);

        TableColumn<Map<String, Object>, String> statCol = new TableColumn<>("Status");
        statCol.setCellValueFactory(c -> new SimpleStringProperty(String.valueOf(c.getValue().getOrDefault("status", ""))));
        statCol.setPrefWidth(80);

        TableColumn<Map<String, Object>, String> msgCol = new TableColumn<>("Message / Outcome");
        msgCol.setCellValueFactory(c -> new SimpleStringProperty(String.valueOf(c.getValue().getOrDefault("message", ""))));
        msgCol.setPrefWidth(195);

        logTable.getColumns().addAll(timeCol, txCol, custCol, chCol, recCol, statCol, msgCol);
        VBox.setVgrow(logTable, Priority.ALWAYS);

        List<Map<String, Object>> logs = uiService.getCommunicationLogs("ALL");
        logTable.setItems(FXCollections.observableArrayList(logs));

        Button closeBtn = new Button("Close");
        closeBtn.getStyleClass().add("button-secondary");
        closeBtn.setOnAction(e -> stage.close());

        HBox bottom = new HBox(closeBtn);
        bottom.setAlignment(Pos.CENTER_RIGHT);

        root.getChildren().addAll(header, sub, logTable, bottom);

        Scene scene = new Scene(root);
        if (getScene() != null && getScene().getStylesheets() != null) {
            scene.getStylesheets().addAll(getScene().getStylesheets());
        }
        stage.setScene(scene);
        stage.showAndWait();
    }
}
