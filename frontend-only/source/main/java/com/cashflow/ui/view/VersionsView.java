package com.cashflow.ui.view;

import com.cashflow.ui.service.CashFlowUiService;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.util.List;
import java.util.Map;

public class VersionsView extends VBox {

    private final CashFlowUiService uiService;
    private final TableView<Map<String, Object>> versionsTable = new TableView<>();
    private final TableView<Map<String, Object>> changesTable = new TableView<>();

    public VersionsView(CashFlowUiService uiService) {
        this.uiService = uiService;
        setSpacing(20);
        setPadding(new Insets(24));
        buildUI();
        loadData();
    }

    private void buildUI() {
        VBox headerBox = new VBox(4);
        Label title = new Label("Dataset Versions & Audit History");
        title.getStyleClass().add("header-title");
        Label subtitle = new Label("Immutable version lineage tracking transaction changes (ADDED, MODIFIED, REMOVED) between CSV uploads.");
        subtitle.getStyleClass().add("header-subtitle");
        headerBox.getChildren().addAll(title, subtitle);

        Label vTitle = new Label("Dataset Versions History");
        vTitle.setStyle("-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: -color-text-primary;");

        setupVersionsTable();
        versionsTable.setPrefHeight(180);

        cTitle.setStyle("-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: -color-text-primary;");

        setupChangesTable();
        changesTable.setPrefHeight(240);
        VBox.setVgrow(changesTable, Priority.ALWAYS);

        getChildren().addAll(headerBox, vTitle, versionsTable, cTitle, changesTable);
    }

    private final Label cTitle = new Label("Recent Transaction Delta (Changes Detected)");

    private void setupVersionsTable() {
        versionsTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<Map<String, Object>, String> numCol = new TableColumn<>("Version #");
        numCol.setCellValueFactory(c -> new SimpleStringProperty("V" + c.getValue().get("versionNumber")));
        numCol.setPrefWidth(90);

        TableColumn<Map<String, Object>, String> fileCol = new TableColumn<>("Source Filename");
        fileCol.setCellValueFactory(c -> new SimpleStringProperty(String.valueOf(c.getValue().get("fileName"))));
        fileCol.setPrefWidth(220);

        TableColumn<Map<String, Object>, String> dateCol = new TableColumn<>("Uploaded At");
        dateCol.setCellValueFactory(c -> new SimpleStringProperty(String.valueOf(c.getValue().get("uploadDate"))));
        dateCol.setPrefWidth(200);

        TableColumn<Map<String, Object>, String> countCol = new TableColumn<>("Total Records");
        countCol.setCellValueFactory(c -> new SimpleStringProperty(String.valueOf(c.getValue().get("recordCount"))));
        countCol.setPrefWidth(120);

        versionsTable.getColumns().addAll(numCol, fileCol, dateCol, countCol);
    }

    private void setupChangesTable() {
        changesTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<Map<String, Object>, String> idCol = new TableColumn<>("Transaction ID");
        idCol.setCellValueFactory(c -> new SimpleStringProperty(String.valueOf(c.getValue().get("transaction_id"))));
        idCol.setPrefWidth(120);

        TableColumn<Map<String, Object>, String> typeCol = new TableColumn<>("Change Type");
        typeCol.setCellValueFactory(c -> new SimpleStringProperty(String.valueOf(c.getValue().get("change_type"))));
        typeCol.setPrefWidth(110);

        TableColumn<Map<String, Object>, String> fieldCol = new TableColumn<>("Field");
        fieldCol.setCellValueFactory(c -> new SimpleStringProperty(String.valueOf(c.getValue().get("field_name"))));
        fieldCol.setPrefWidth(120);

        TableColumn<Map<String, Object>, String> oldCol = new TableColumn<>("Previous Value");
        oldCol.setCellValueFactory(c -> new SimpleStringProperty(String.valueOf(c.getValue().get("old_value"))));
        oldCol.setPrefWidth(150);

        TableColumn<Map<String, Object>, String> newCol = new TableColumn<>("New Value");
        newCol.setCellValueFactory(c -> new SimpleStringProperty(String.valueOf(c.getValue().get("new_value"))));
        newCol.setPrefWidth(150);

        changesTable.getColumns().addAll(idCol, typeCol, fieldCol, oldCol, newCol);

        versionsTable.getSelectionModel().selectedItemProperty().addListener((obs, oldV, newV) -> {
            if (newV != null) {
                String versionId = String.valueOf(newV.get("versionId"));
                Map<String, Object> diff = uiService.compareVersions(null, versionId);
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> changes = (List<Map<String, Object>>) diff.getOrDefault("changes", List.of());
                changesTable.setItems(FXCollections.observableArrayList(changes));
                cTitle.setText("Transaction Delta for V" + newV.get("versionNumber") + " (" + changes.size() + " change(s) detected)");
            }
        });
    }

    @SuppressWarnings("unchecked")
    public void loadData() {
        List<Map<String, Object>> versions = uiService.getVersions();
        versionsTable.setItems(FXCollections.observableArrayList(versions));

        if (!versions.isEmpty()) {
            versionsTable.getSelectionModel().select(0);
            String latestId = String.valueOf(versions.get(0).get("versionId"));
            Map<String, Object> diff = uiService.compareVersions(null, latestId);
            List<Map<String, Object>> changes = (List<Map<String, Object>>) diff.getOrDefault("changes", List.of());
            changesTable.setItems(FXCollections.observableArrayList(changes));
            cTitle.setText("Transaction Delta for V" + versions.get(0).get("versionNumber") + " (" + changes.size() + " change(s) detected)");
        }
    }
}
