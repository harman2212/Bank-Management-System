package com.bank.app.model;

public class SavingsGoal {
    private static int goalCounter = 1;
    private final int id;
    private final String name;
    private final String category;
    private final double targetAmount;
    private final double currentAmount;
    private final String targetDate;

    public SavingsGoal(String name, String category, double targetAmount, double currentAmount, String targetDate) {
        this.id = goalCounter++;
        this.name = name;
        this.category = category;
        this.targetAmount = targetAmount;
        this.currentAmount = currentAmount;
        this.targetDate = targetDate;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getCategory() {
        return category;
    }

    public double getTargetAmount() {
        return targetAmount;
    }

    public double getCurrentAmount() {
        return currentAmount;
    }

    public String getTargetDate() {
        return targetDate;
    }

    public double getRemainingAmount() {
        return Math.max(0, targetAmount - currentAmount);
    }

    public int getCompletionPercent() {
        if (targetAmount <= 0) {
            return 0;
        }
        return (int) Math.min(100, Math.round((currentAmount / targetAmount) * 100));
    }
}
