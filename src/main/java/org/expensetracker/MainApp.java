package org.expensetracker;

import javafx.application.Application;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.scene.control.cell.PropertyValueFactory;
import java.util.List;

public class MainApp extends Application {

    private ComboBox<CategoryManager> categoryComboBox;
    private TextField expenseNameField, amountField, categoryNameField, categoryBudgetField;
    private Label statusLabel;
    private TableView<Expense> tableView;
    private ObservableList<Expense> expensesList;

    private TableView<CategoryManager> categorySummaryTable;
    private ObservableList<CategoryManager> categoriesList;

    @Override
    public void start(Stage primaryStage) {
        DatabaseHelper.createDatabase();

        VBox root = new VBox(10);
        root.setPadding(new Insets(10));

        categoryNameField = new TextField();
        categoryNameField.setPromptText("Category Name");

        categoryBudgetField = new TextField();
        categoryBudgetField.setPromptText("Category Budget");

        Button addCategoryButton = new Button("Add Category");
        addCategoryButton.setOnAction(e -> addCategory());

        HBox categoryBox = new HBox(10, categoryNameField, categoryBudgetField, addCategoryButton);

        categoryComboBox = new ComboBox<>();
        refreshCategories();

        expenseNameField = new TextField();
        expenseNameField.setPromptText("Expense Name");

        amountField = new TextField();
        amountField.setPromptText("Amount");

        Button addExpenseButton = new Button("Add Expense");
        addExpenseButton.setOnAction(e -> addExpense());

        Button exportButton = new Button("Export Selected Category to PDF");
        exportButton.setOnAction(e -> exportSelectedCategoryToPDF());

        statusLabel = new Label();

        tableView = new TableView<>();
        TableColumn<Expense, String> nameCol = new TableColumn<>("Name");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));

        TableColumn<Expense, Double> amountCol = new TableColumn<>("Amount");
        amountCol.setCellValueFactory(new PropertyValueFactory<>("amount"));

        TableColumn<Expense, String> categoryCol = new TableColumn<>("Category");
        categoryCol.setCellValueFactory(new PropertyValueFactory<>("categoryName"));

        tableView.getColumns().addAll(nameCol, amountCol, categoryCol);

        Button deleteButton = new Button("Delete Selected");
        deleteButton.setOnAction(e -> deleteSelectedExpense());

        expensesList = FXCollections.observableArrayList();
        tableView.setItems(expensesList);

        // Category Summary Table
        categorySummaryTable = new TableView<>();
        TableColumn<CategoryManager, String> catNameCol = new TableColumn<>("Category");
        catNameCol.setCellValueFactory(new PropertyValueFactory<>("name"));

        TableColumn<CategoryManager, Double> catBudgetCol = new TableColumn<>("Budget");
        catBudgetCol.setCellValueFactory(new PropertyValueFactory<>("budget"));

        TableColumn<CategoryManager, Double> catSpentCol = new TableColumn<>("Spent");
        catSpentCol.setCellValueFactory(param ->
            new ReadOnlyObjectWrapper<>(DatabaseHelper.getTotalSpentInCategory(param.getValue().getId()))
        );

        TableColumn<CategoryManager, Double> catRemainingCol = new TableColumn<>("Remaining");
        catRemainingCol.setCellValueFactory(param -> {
            double spent = DatabaseHelper.getTotalSpentInCategory(param.getValue().getId());
            double remaining = param.getValue().getBudget() - spent;
            return new ReadOnlyObjectWrapper<>(remaining);
        });

        categorySummaryTable.getColumns().addAll(catNameCol, catBudgetCol, catSpentCol, catRemainingCol);

        categoriesList = FXCollections.observableArrayList();
        categorySummaryTable.setItems(categoriesList);

        refreshTable();
        refreshCategorySummary();

        root.getChildren().addAll(
                categoryBox, categoryComboBox, expenseNameField, amountField,
                addExpenseButton, tableView, deleteButton, exportButton,
                categorySummaryTable, statusLabel
        );

        Scene scene = new Scene(root, 800, 600);
        primaryStage.setTitle("Personal Expense Tracker");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void addCategory() {
        String name = categoryNameField.getText().trim();
        String budgetText = categoryBudgetField.getText().trim();
        if (name.isEmpty() || budgetText.isEmpty()) {
            statusLabel.setText("Fill category name and budget.");
            return;
        }
        try {
            double budget = Double.parseDouble(budgetText);
            DatabaseHelper.insertCategory(name, budget);
            refreshCategories();
            refreshCategorySummary();
            categoryNameField.clear();
            categoryBudgetField.clear();
            statusLabel.setText("Category added successfully.");
        } catch (NumberFormatException e) {
            statusLabel.setText("Invalid budget amount.");
        }
    }

    private void addExpense() {
        String expenseName = expenseNameField.getText().trim();
        String amountText = amountField.getText().trim();
        CategoryManager selectedCategory = categoryComboBox.getValue();

        if (expenseName.isEmpty() || amountText.isEmpty() || selectedCategory == null) {
            statusLabel.setText("Fill expense name, amount, and select category.");
            return;
        }

        try {
            double amount = Double.parseDouble(amountText);
            double totalSpent = DatabaseHelper.getTotalSpentInCategory(selectedCategory.getId());

            if (totalSpent + amount > selectedCategory.getBudget()) {
                statusLabel.setText("Warning! Expense exceeds category budget.");
            } else {
                statusLabel.setText("Expense added successfully.");
            }
            DatabaseHelper.insertExpense(expenseName, amount, selectedCategory.getId());
            refreshTable();
            refreshCategorySummary();
            expenseNameField.clear();
            amountField.clear();
        } catch (NumberFormatException e) {
            statusLabel.setText("Invalid amount.");
        }
    }

    private void deleteSelectedExpense() {
        Expense selected = tableView.getSelectionModel().getSelectedItem();
        if (selected != null) {
            DatabaseHelper.deleteExpense(selected.getId());
            statusLabel.setText("Expense deleted.");
            refreshTable();
            refreshCategorySummary();
        } else {
            statusLabel.setText("Select an expense to delete.");
        }
    }

    private void refreshCategories() {
        categoryComboBox.getItems().clear();
        categoryComboBox.getItems().addAll(DatabaseHelper.getAllCategories());
    }

    private void refreshTable() {
        expensesList.setAll(DatabaseHelper.getAllExpenses());
    }

    private void refreshCategorySummary() {
        categoriesList.setAll(DatabaseHelper.getAllCategories());
    }

    private void exportSelectedCategoryToPDF() {
    	CategoryManager selected = categoryComboBox.getValue();
        if (selected != null) {
            PDFExporter.exportCategoryExpenses(selected);
            statusLabel.setText("PDF Exported for category: " + selected.getName());
        } else {
            statusLabel.setText("Please select a category to export.");
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
