package com.cashflow.ui.view;

import com.cashflow.model.Transaction;
import com.cashflow.ui.service.CashFlowUiService;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.util.List;
import java.util.Locale;

public class TransactionsView extends VBox {

    private final CashFlowUiService uiService;
    private final TableView<Transaction> table = new TableView<>();
    private final TextField searchField = new TextField();
    private final ComboBox<String> typeFilter = new ComboBox<>();
    private final ComboBox<String> statusFilter = new ComboBox<>();
    private final Label countLabel = new Label();
    private final NumberFormat currencyFormatter = NumberFormat.getCurrencyInstance(new Locale("en", "IN"));

    public TransactionsView(CashFlowUiService uiService) {
        this.uiService = uiService;
        setSpacing(16);
        setPadding(new Insets(24));
        buildUI();
        loadData();
    }

    private void buildUI() {
        // Header
        VBox headerBox = new VBox(4);
        Label title = new Label("Transaction Ledger & Audit");
        title.getStyleClass().add("header-title");
        Label subtitle = new Label("Inspect, filter, search, and verify all imported income and expense line items.");
        subtitle.getStyleClass().add("header-subtitle");
        headerBox.getChildren().addAll(title, subtitle);

        // Filter Bar
        HBox filterBar = new HBox(12);
        filterBar.setAlignment(Pos.CENTER_LEFT);

        searchField.setPromptText("Search description, customer, phone, email, supplier, ID...");
        searchField.setPrefWidth(300);
        searchField.textProperty().addListener((obs, oldVal, newVal) -> loadData());

        typeFilter.getItems().addAll("ALL", "INCOME", "EXPENSE");
        typeFilter.setValue("ALL");
        typeFilter.valueProperty().addListener((obs, oldVal, newVal) -> loadData());

        statusFilter.getItems().addAll("ALL", "PAID", "PENDING", "OVERDUE");
        statusFilter.setValue("ALL");
        statusFilter.valueProperty().addListener((obs, oldVal, newVal) -> loadData());

        Button refreshBtn = new Button("Refresh");
        refreshBtn.getStyleClass().add("button-secondary");
        refreshBtn.setOnAction(e -> loadData());

        countLabel.setStyle("-fx-text-fill: -color-text-secondary; -fx-font-size: 12px;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        filterBar.getChildren().addAll(
                new Label("Search:"), searchField,
                new Label("Type:"), typeFilter,
                new Label("Status:"), statusFilter,
                refreshBtn,
                spacer,
                countLabel
        );

        // Table setup
        setupTable();
        VBox.setVgrow(table, Priority.ALWAYS);

        getChildren().addAll(headerBox, filterBar, table);
    }

    private void setupTable() {
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<Transaction, String> idCol = new TableColumn<>("TXN ID");
        idCol.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getTransactionId()));
        idCol.setPrefWidth(90);

        TableColumn<Transaction, LocalDate> dateCol = new TableColumn<>("Date");
        dateCol.setCellValueFactory(c -> new SimpleObjectProperty<>(c.getValue().getTransactionDate()));
        dateCol.setPrefWidth(95);

        TableColumn<Transaction, String> typeCol = new TableColumn<>("Type");
        typeCol.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getTransactionType()));
        typeCol.setPrefWidth(80);

        TableColumn<Transaction, String> categoryCol = new TableColumn<>("Category");
        categoryCol.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getCategory()));
        categoryCol.setPrefWidth(110);

        TableColumn<Transaction, String> descCol = new TableColumn<>("Description");
        descCol.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getDescription()));
        descCol.setPrefWidth(200);

        TableColumn<Transaction, String> entityCol = new TableColumn<>("Party / Contact");
        entityCol.setCellValueFactory(c -> {
            Transaction t = c.getValue();
            String entity = t.getCustomer();
            if (entity == null || entity.isEmpty()) entity = t.getSupplier();
            if (entity == null) entity = "—";
            if (t.getCustomerPhone() != null && !t.getCustomerPhone().isBlank()) {
                entity += " (" + t.getCustomerPhone() + ")";
            }
            return new SimpleStringProperty(entity);
        });
        entityCol.setPrefWidth(180);

        TableColumn<Transaction, String> amountCol = new TableColumn<>("Amount");
        amountCol.setCellValueFactory(c -> new SimpleStringProperty(formatAmount(c.getValue().getAmount())));
        amountCol.setPrefWidth(120);
        amountCol.setStyle("-fx-alignment: CENTER-RIGHT;");

        TableColumn<Transaction, String> statusCol = new TableColumn<>("Status");
        statusCol.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getStatus()));
        statusCol.setPrefWidth(100);

        table.getColumns().addAll(idCol, dateCol, typeCol, categoryCol, descCol, entityCol, amountCol, statusCol);
    }

    public void loadData() {
        String search = searchField.getText();
        String type = typeFilter.getValue();
        String status = statusFilter.getValue();

        List<Transaction> list = uiService.getTransactions(search, type, "ALL", status);
        table.setItems(FXCollections.observableArrayList(list));
        countLabel.setText(list.size() + " record(s) loaded");
    }

    private String formatAmount(BigDecimal amount) {
        if (amount == null) return "₹0.00";
        return currencyFormatter.format(amount);
    }
}
