package com.bank.app.model;

import java.util.ArrayList;
import java.util.List;

public class Account {
    private static int accountCounter = 1000000000; // Start at 10-digit number
    private int accountNumber;
    private double balance;
    private String accountType; // Checking or Savings
    private List<Transaction> transactions;

    public Account(String accountType, double initialBalance) {
        this.accountNumber = accountCounter++;
        this.accountType = accountType;
        this.balance = initialBalance;
        this.transactions = new ArrayList<>();
        if (initialBalance > 0) {
            this.transactions.add(new Transaction("Initial deposit", "Deposit", initialBalance, this.balance));
        }
    }

    // Getters and Setters
    public int getAccountNumber() {
        return accountNumber;
    }

    public double getBalance() {
        return balance;
    }

    public String getAccountType() {
        return accountType;
    }

    public List<Transaction> getTransactions() {
        return transactions;
    }

    public void deposit(double amount) {
        if (amount > 0) {
            this.balance += amount;
            transactions.add(new Transaction("Deposit", "Deposit", amount, this.balance));
        }
    }

    public boolean withdraw(double amount) {
        if (amount > 0 && amount <= this.balance) {
            this.balance -= amount;
            transactions.add(new Transaction("Withdrawal", "Withdrawal", amount, this.balance));
            return true;
        }
        return false;
    }

    public boolean transfer(Account targetAccount, double amount) {
        if (amount > 0 && amount <= this.balance) {
            this.balance -= amount;
            targetAccount.balance += amount;
            this.transactions.add(new Transaction("Transfer to account " + targetAccount.getAccountNumber(), "Transfer", amount, this.balance));
            targetAccount.transactions.add(new Transaction("Transfer from account " + this.getAccountNumber(), "Transfer", amount, targetAccount.balance));
            return true;
        }
        return false;
    }
}
