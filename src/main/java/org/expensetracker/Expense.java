package org.expensetracker;

public class Expense {
    private int id;
    private String name;
    private double amount;
    private String categoryName;

    public Expense(int id, String name, double amount, String categoryName) {
        this.id = id;
        this.name = name;
        this.amount = amount;
        this.categoryName = categoryName;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public double getAmount() {
        return amount;
    }

    public String getCategoryName() {
        return categoryName;
    }
}
