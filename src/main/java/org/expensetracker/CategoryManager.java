package org.expensetracker;

public class CategoryManager {
    private int id;
    private String name;
    private double budget;

    public CategoryManager(int id, String name, double budget) {
        this.id = id;
        this.name = name;
        this.budget = budget;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public double getBudget() {
        return budget;
    }

    @Override
    public String toString() {
        return name + " (Budget: " + budget + ")";
    }
}
