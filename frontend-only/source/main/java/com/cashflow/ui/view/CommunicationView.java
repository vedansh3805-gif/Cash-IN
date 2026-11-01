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

/**
 * Dedicated Communication Page for viewing customer contact details (mobile phone & email),
 * initiating calls, dispatching SMS and email notices, and reviewing complete dual-party audit logs.
 */
public class CommunicationView extends VBox {

    private final CashFlowUiService uiService;
    private final TableView<Transaction> customerTable = new TableView<>();
    private final TableView<Map<String, Object>> logTable = new TableView<>();
    private final HBox kpiBox = new HBox(16);
    private final NumberFormat currencyFormatter = NumberFormat.getCurrencyInstance(new Locale("en", "IN"));
    private final ObservableList<Transaction> masterData = FXCollections.observableArrayList();
    private final ObservableList<Map<String, Object>> masterLogs = FXCollections.observableArrayList();
    private String currentStatusFilter = "ALL";
    private String currentSearch = "";

    public CommunicationView(CashFlowUiService uiService) {
        this.uiService = uiService;
        setSpacing(20);
        setPadding(new Insets(24));
        buildUI();
        loadData();
    }

    private void buildUI() {
        VBox headerBox = new VBox(4);
        Label title = new Label("Customer Communication & Multi-Channel Outreach");
        title.getStyleClass().add("header-title");
        Label subtitle = new Label("Manage customer mobile numbers and email addresses. Initiate phone calls, dispatch automated SMS reminders, send formal billing emails, and inspect complete dual-party audit logs.");
        subtitle.getStyleClass().add("header-subtitle");
        headerBox.getChildren().addAll(title, subtitle);

        // Filter & Action Toolbar
        HBox toolbar = new HBox(12);
        toolbar.setAlignment(Pos.CENTER_LEFT);

        TextField searchField = new TextField();
        searchField.setPromptText("🔍 Search Customer, Phone, Email, Invoice...");
        searchField.setPrefWidth(260);
        searchField.textProperty().addListener((obs, oldVal, newVal) -> {
            currentSearch = newVal != null ? newVal.trim().toLowerCase() : "";
            applyCustomerFilter();
        });

        ComboBox<String> filterCombo = new ComboBox<>();
        filterCombo.getItems().addAll("ALL INVOICES", "OVERDUE DEFAULTERS ONLY", "PENDING ONLY");
        filterCombo.setValue("ALL INVOICES");
        filterCombo.setOnAction(e -> {
            String val = filterCombo.getValue();
            if ("OVERDUE DEFAULTERS ONLY".equals(val)) {
                currentStatusFilter = "OVERDUE";
            } else if ("PENDING ONLY".equals(val)) {
                currentStatusFilter = "PENDING";
            } else {
                currentStatusFilter = "ALL";
            }
            applyCustomerFilter();
        });

        Button bulkAlertBtn = new Button("⚡ Alert All Defaulters (Dual-Party SMS & Email)");
        bulkAlertBtn.getStyleClass().add("button-primary");
        bulkAlertBtn.setOnAction(e -> notifyAllDefaulters());

        Button refreshBtn = new Button("Refresh");
        refreshBtn.getStyleClass().add("button-secondary");
        refreshBtn.setOnAction(e -> loadData());

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        toolbar.getChildren().addAll(
                searchField,
                new Label("Status:"), filterCombo,
                spacer,
                bulkAlertBtn,
                refreshBtn
        );

        // TabPane for Customers vs Communication Audit Log
        TabPane tabPane = new TabPane();
        tabPane.getStyleClass().add("plain-tabpane");
        VBox.setVgrow(tabPane, Priority.ALWAYS);

        Tab customerTab = new Tab("Customer Contact Directory & Actions");
        customerTab.setClosable(false);
        setupCustomerTable();
        customerTab.setContent(customerTable);

        Tab auditTab = new Tab("Communication & Call History Audit Trail");
        auditTab.setClosable(false);
        setupLogTable();
        auditTab.setContent(logTable);

        tabPane.getTabs().addAll(customerTab, auditTab);

        getChildren().addAll(headerBox, kpiBox, toolbar, tabPane);
    }

    private void setupCustomerTable() {
        customerTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<Transaction, String> idCol = new TableColumn<>("Invoice ID");
        idCol.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getTransactionId()));
        idCol.setPrefWidth(85);

        TableColumn<Transaction, String> custCol = new TableColumn<>("Customer / Buyer");
        custCol.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getCustomer() != null ? c.getValue().getCustomer() : "—"));
        custCol.setPrefWidth(140);

        TableColumn<Transaction, String> phoneCol = new TableColumn<>("Mobile Number");
        phoneCol.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getCustomerPhone() != null && !c.getValue().getCustomerPhone().isBlank() ? c.getValue().getCustomerPhone() : "—"));
        phoneCol.setPrefWidth(130);

        TableColumn<Transaction, String> emailCol = new TableColumn<>("Email Address");
        emailCol.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getCustomerEmail() != null && !c.getValue().getCustomerEmail().isBlank() ? c.getValue().getCustomerEmail() : "—"));
        emailCol.setPrefWidth(160);

        TableColumn<Transaction, LocalDate> dueCol = new TableColumn<>("Due Date");
        dueCol.setCellValueFactory(c -> new SimpleObjectProperty<>(c.getValue().getDueDate()));
        dueCol.setPrefWidth(95);

        TableColumn<Transaction, String> amtCol = new TableColumn<>("Outstanding");
        amtCol.setCellValueFactory(c -> new SimpleStringProperty(formatAmount(c.getValue().getAmount())));
        amtCol.setPrefWidth(110);
        amtCol.setStyle("-fx-alignment: CENTER-RIGHT; -fx-font-weight: bold;");

        TableColumn<Transaction, String> statusCol = new TableColumn<>("Status");
        statusCol.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getStatus()));
        statusCol.setPrefWidth(95);
        statusCol.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String status, boolean empty) {
                super.updateItem(status, empty);
                if (empty || status == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    Label badge = new Label(status);
                    badge.getStyleClass().add("badge");
                    if ("OVERDUE".equalsIgnoreCase(status)) {
                        badge.getStyleClass().add("badge-danger");
                    } else if ("PAID".equalsIgnoreCase(status)) {
                        badge.getStyleClass().add("badge-success");
                    } else {
                        badge.getStyleClass().add("badge-warning");
                    }
                    setGraphic(badge);
                    setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
                    setAlignment(Pos.CENTER);
                }
            }
        });

        TableColumn<Transaction, Void> actionCol = new TableColumn<>("Outreach Actions (Call / SMS / Email)");
        actionCol.setPrefWidth(300);
        actionCol.setCellFactory(col -> new TableCell<>() {
            private final Button callBtn = new Button("📞 Call");
            private final Button smsBtn = new Button("💬 SMS");
            private final Button emailBtn = new Button("✉️ Email");
            private final Button notifyBothBtn = new Button("🔄 Both");
            private final HBox pane = new HBox(6, callBtn, smsBtn, emailBtn, notifyBothBtn);

            {
                pane.setAlignment(Pos.CENTER);
                callBtn.getStyleClass().add("button-secondary");
                smsBtn.getStyleClass().add("button-secondary");
                emailBtn.getStyleClass().add("button-secondary");
                notifyBothBtn.getStyleClass().add("button-primary");

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
                notifyBothBtn.setOnAction(e -> {
                    Transaction t = getTableView().getItems().get(getIndex());
                    triggerNotifyBoth(t);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : pane);
            }
        });

        customerTable.getColumns().addAll(idCol, custCol, phoneCol, emailCol, dueCol, amtCol, statusCol, actionCol);
    }

    private void setupLogTable() {
        logTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<Map<String, Object>, String> timeCol = new TableColumn<>("Timestamp");
        timeCol.setCellValueFactory(c -> new SimpleStringProperty(String.valueOf(c.getValue().getOrDefault("timestamp", "—"))));
        timeCol.setPrefWidth(140);

        TableColumn<Map<String, Object>, String> txnCol = new TableColumn<>("Invoice ID");
        txnCol.setCellValueFactory(c -> new SimpleStringProperty(String.valueOf(c.getValue().getOrDefault("transactionId", "—"))));
        txnCol.setPrefWidth(90);

        TableColumn<Map<String, Object>, String> custCol = new TableColumn<>("Customer");
        custCol.setCellValueFactory(c -> new SimpleStringProperty(String.valueOf(c.getValue().getOrDefault("customerName", "—"))));
        custCol.setPrefWidth(120);

        TableColumn<Map<String, Object>, String> channelCol = new TableColumn<>("Channel");
        channelCol.setCellValueFactory(c -> new SimpleStringProperty(String.valueOf(c.getValue().getOrDefault("channel", "—"))));
        channelCol.setPrefWidth(85);
        channelCol.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String channel, boolean empty) {
                super.updateItem(channel, empty);
                if (empty || channel == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    Label badge = new Label(channel);
                    badge.getStyleClass().add("badge");
                    if ("CALL".equalsIgnoreCase(channel)) badge.getStyleClass().add("badge-info");
                    else if ("SMS".equalsIgnoreCase(channel)) badge.getStyleClass().add("badge-warning");
                    else badge.getStyleClass().add("badge-success");
                    setGraphic(badge);
                    setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
                    setAlignment(Pos.CENTER);
                }
            }
        });

        TableColumn<Map<String, Object>, String> recipCol = new TableColumn<>("Recipient");
        recipCol.setCellValueFactory(c -> new SimpleStringProperty(
                String.valueOf(c.getValue().getOrDefault("recipientType", "")) + " (" +
                        String.valueOf(c.getValue().getOrDefault("recipientContact", "")) + ")"
        ));
        recipCol.setPrefWidth(180);

        TableColumn<Map<String, Object>, String> subjCol = new TableColumn<>("Subject / Action");
        subjCol.setCellValueFactory(c -> new SimpleStringProperty(String.valueOf(c.getValue().getOrDefault("subject", "—"))));
        subjCol.setPrefWidth(150);

        TableColumn<Map<String, Object>, String> msgCol = new TableColumn<>("Message / Notes");
        msgCol.setCellValueFactory(c -> new SimpleStringProperty(String.valueOf(c.getValue().getOrDefault("message", "—"))));
        msgCol.setPrefWidth(220);

        TableColumn<Map<String, Object>, String> statusCol = new TableColumn<>("Status");
        statusCol.setCellValueFactory(c -> new SimpleStringProperty(String.valueOf(c.getValue().getOrDefault("status", "—"))));
        statusCol.setPrefWidth(85);

        logTable.getColumns().addAll(timeCol, txnCol, custCol, channelCol, recipCol, subjCol, msgCol, statusCol);
    }

    private void loadData() {
        masterData.clear();
        masterLogs.clear();

        List<Transaction> all = uiService.getTransactions(null, "INCOME", null, null);
        List<Transaction> receivables = all.stream()
                .filter(t -> "PENDING".equalsIgnoreCase(t.getStatus()) || "OVERDUE".equalsIgnoreCase(t.getStatus()))
                .collect(Collectors.toList());

        masterData.addAll(receivables);
        applyCustomerFilter();

        List<Map<String, Object>> logs = uiService.getCommunicationLogs(null);
        masterLogs.addAll(logs);
        logTable.setItems(masterLogs);

        updateKpiCards(receivables, logs);
    }

    private void updateKpiCards(List<Transaction> receivables, List<Map<String, Object>> logs) {
        kpiBox.getChildren().clear();

        BigDecimal totalOutstanding = receivables.stream()
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        long overdueCount = receivables.stream()
                .filter(t -> "OVERDUE".equalsIgnoreCase(t.getStatus()))
                .count();

        long callCount = logs.stream()
                .filter(l -> "CALL".equalsIgnoreCase(String.valueOf(l.get("channel"))))
                .count();

        long smsCount = logs.stream()
                .filter(l -> "SMS".equalsIgnoreCase(String.valueOf(l.get("channel"))))
                .count();

        long emailCount = logs.stream()
                .filter(l -> "EMAIL".equalsIgnoreCase(String.valueOf(l.get("channel"))))
                .count();

        kpiBox.getChildren().addAll(
                createKpiCard("Total Debtors", String.valueOf(receivables.size()), "Pending receivables invoices", "default"),
                createKpiCard("Overdue Defaulters", String.valueOf(overdueCount), "Immediate action required", overdueCount > 0 ? "danger" : "success"),
                createKpiCard("Total Outstanding", formatAmount(totalOutstanding), "Gross pending balance", "default"),
                createKpiCard("Calls Placed", String.valueOf(callCount), "Outbound phone calls", "accent"),
                createKpiCard("SMS Notices", String.valueOf(smsCount), "Dual-party reminders", "accent"),
                createKpiCard("Emails Sent", String.valueOf(emailCount), "Formal billing notices", "accent")
        );
    }

    private VBox createKpiCard(String label, String value, String sub, String theme) {
        VBox card = new VBox(6);
        card.getStyleClass().add("kpi-card");
        HBox.setHgrow(card, Priority.ALWAYS);

        Label l = new Label(label);
        l.getStyleClass().add("kpi-label");

        Label v = new Label(value);
        v.getStyleClass().add("kpi-value");
        if ("danger".equals(theme)) v.setStyle("-fx-text-fill: -color-danger;");
        else if ("success".equals(theme)) v.setStyle("-fx-text-fill: -color-success;");
        else if ("accent".equals(theme)) v.setStyle("-fx-text-fill: -color-accent;");

        Label s = new Label(sub);
        s.getStyleClass().add("kpi-sub");

        card.getChildren().addAll(l, v, s);
        return card;
    }

    private void applyCustomerFilter() {
        List<Transaction> filtered = masterData.stream().filter(t -> {
            if ("OVERDUE".equalsIgnoreCase(currentStatusFilter) && !"OVERDUE".equalsIgnoreCase(t.getStatus())) return false;
            if ("PENDING".equalsIgnoreCase(currentStatusFilter) && !"PENDING".equalsIgnoreCase(t.getStatus())) return false;

            if (!currentSearch.isEmpty()) {
                String cust = t.getCustomer() != null ? t.getCustomer().toLowerCase() : "";
                String phone = t.getCustomerPhone() != null ? t.getCustomerPhone().toLowerCase() : "";
                String email = t.getCustomerEmail() != null ? t.getCustomerEmail().toLowerCase() : "";
                String id = t.getTransactionId() != null ? t.getTransactionId().toLowerCase() : "";
                return cust.contains(currentSearch) || phone.contains(currentSearch) || email.contains(currentSearch) || id.contains(currentSearch);
            }
            return true;
        }).collect(Collectors.toList());

        customerTable.setItems(FXCollections.observableArrayList(filtered));
    }

    private String formatAmount(BigDecimal amount) {
        if (amount == null) return "₹0.00";
        return currencyFormatter.format(amount);
    }

    private void triggerNotifyBoth(Transaction t) {
        try {
            Map<String, Object> res = uiService.notifyBothParties(t.getTransactionId());
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Dual-Party Notice Dispatched");
            alert.setHeaderText("Dispatched to Buyer & Shopkeeper!");
            alert.setContentText(String.valueOf(res.getOrDefault("message", "SMS & Email sent.")));
            alert.showAndWait();
            loadData();
        } catch (Exception ex) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Dispatch Failed");
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
        confirm.setContentText("This will send SMS reminders and Email notices to each buyer's mobile/email AND audit copies to the shopkeeper.");
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
            success.setTitle("Outreach Complete");
            success.setHeaderText("Dispatched " + count + " Dual-Party Notices");
            success.setContentText("Successfully alerted " + count + " overdue accounts via SMS & Email.");
            success.showAndWait();
            loadData();
        }
    }

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
        TextArea notesArea = new TextArea("Discussed overdue payment. Customer confirmed remittance within 48 hours.");
        notesArea.setPrefRowCount(3);

        Label statusLbl = new Label();
        statusLbl.setStyle("-fx-font-weight: bold; -fx-text-fill: -color-accent;");

        Button dialBtn = new Button("📞 Place Call & Log Event");
        dialBtn.getStyleClass().add("button-primary");
        dialBtn.setOnAction(e -> {
            try {
                Map<String, Object> res = uiService.initiateCall(t.getTransactionId(), notesArea.getText().trim());
                statusLbl.setText("✓ " + res.getOrDefault("message", "Call logged to audit trail."));
                dialBtn.setDisable(true);
                loadData();
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

    private void showSmsDialog(Transaction t) {
        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setTitle("Dispatch SMS Reminder Notice — Invoice #" + t.getTransactionId());

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

        Label shopkeeperLabel = new Label("2. Shopkeeper Audit Copy (to Shopkeeper at " + shopkeeperPhone + "):");
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
                statusLbl.setText("✓ " + res.getOrDefault("message", "SMS dispatched to both parties."));
                sendBtn.setDisable(true);
                loadData();
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

    private void showEmailDialog(Transaction t) {
        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setTitle("Formal Invoice Billing Notice — Invoice #" + t.getTransactionId());

        VBox root = new VBox(14);
        root.setPadding(new Insets(20));
        root.setPrefWidth(580);

        Label header = new Label("Dispatch Formal Email Notice (Dual-Party Audit)");
        header.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        Map<String, Object> settings = uiService.getSettings();
        String shopkeeperEmail = String.valueOf(settings.getOrDefault("shopkeeperEmail", "shopkeeper@cashflow.com"));
        String shopkeeperPhone = String.valueOf(settings.getOrDefault("shopkeeperPhone", "+91 98765 00000"));
        String customer = t.getCustomer() != null ? t.getCustomer() : "Valued Customer";
        String buyerEmail = t.getCustomerEmail() != null && !t.getCustomerEmail().isBlank() ? t.getCustomerEmail() : "accounts@" + customer.toLowerCase().replaceAll("[^a-z0-9]", "") + ".com";

        TextField subjectField = new TextField("URGENT: Outstanding Balance Reminder for Invoice #" + t.getTransactionId());

        TextArea bodyArea = new TextArea(
                String.format(
                        "Dear %s Accounts Team,\n\n" +
                                "This is a formal payment notice regarding your outstanding balance with CashFlow Business.\n\n" +
                                "Invoice Details:\n" +
                                "• Invoice ID: %s\n" +
                                "• Outstanding Amount: %s\n" +
                                "• Due Date: %s\n" +
                                "• Current Status: %s\n\n" +
                                "Please arrange for payment remittance immediately to ensure uninterrupted trade credit.\n\n" +
                                "Accounts Contact:\n" +
                                "Phone: %s | Email: %s\n\n" +
                                "A copy of this notice is automatically archived in our store ledger for mutual accounting.",
                        customer, t.getTransactionId(), formatAmount(t.getAmount()), t.getDueDate(), t.getStatus(),
                        shopkeeperPhone, shopkeeperEmail
                )
        );
        bodyArea.setPrefRowCount(10);

        Label auditNote = new Label("Audit Copy: Dispatched to " + buyerEmail + " and CC to " + shopkeeperEmail);
        auditNote.setStyle("-fx-font-style: italic; -fx-font-size: 11px;");

        Label statusLbl = new Label();
        statusLbl.setStyle("-fx-font-weight: bold; -fx-text-fill: -color-accent;");

        Button sendBtn = new Button("✉️ Send Formal Notice & Log Audit");
        sendBtn.getStyleClass().add("button-primary");
        sendBtn.setOnAction(e -> {
            try {
                Map<String, Object> res = uiService.sendEmailNotice(t.getTransactionId(), subjectField.getText().trim(), bodyArea.getText().trim());
                statusLbl.setText("✓ " + res.getOrDefault("message", "Email delivered to both parties."));
                sendBtn.setDisable(true);
                loadData();
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

        root.getChildren().addAll(header, new Label("Subject:"), subjectField, new Label("Statement Body:"), bodyArea, auditNote, statusLbl, btnRow);

        Scene scene = new Scene(root);
        if (getScene() != null && getScene().getStylesheets() != null) {
            scene.getStylesheets().addAll(getScene().getStylesheets());
        }
        dialog.setScene(scene);
        dialog.showAndWait();
    }
}
