package com.bank.app.service;

import com.bank.app.model.Account;
import com.bank.app.model.Customer;
import com.bank.app.model.OperationResult;
import com.bank.app.model.SavingsGoal;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class BankService {
    private final Map<Integer, Customer> customers = new HashMap<>();
    private final Pattern emailPattern = Pattern.compile("^.+@.+\\..+$");
    private final List<SavingsGoal> savingsGoals = new ArrayList<>();
    private final Deque<String> activityLog = new ArrayDeque<>();

    // Add sample data for analytics demonstration
    public BankService() {
        // Create sample customers with accounts
        createCustomer("John Doe", "john.doe@email.com", "1234567890");
        createCustomer("Jane Smith", "jane.smith@email.com", "0987654321");
        createCustomer("Bob Johnson", "bob.johnson@email.com", "1122334455");
        createCustomer("Alice Brown", "alice.brown@email.com", "5566778899");
        createCustomer("Charlie Wilson", "charlie.wilson@email.com", "9988776655");

        // Add accounts with different balances
        Customer john = customers.get(1);
        if (john != null) {
            createAccount(john.getId(), "Checking", 2500.00);
            createAccount(john.getId(), "Savings", 15000.00);
        }

        Customer jane = customers.get(2);
        if (jane != null) {
            createAccount(jane.getId(), "Checking", 3200.00);
            createAccount(jane.getId(), "Savings", 8500.00);
        }

        Customer bob = customers.get(3);
        if (bob != null) {
            createAccount(bob.getId(), "Checking", 450.00);
            createAccount(bob.getId(), "Savings", 25000.00);
        }

        Customer alice = customers.get(4);
        if (alice != null) {
            createAccount(alice.getId(), "Checking", 1200.00);
        }

        Customer charlie = customers.get(5);
        if (charlie != null) {
            createAccount(charlie.getId(), "Savings", 7500.00);
        }

        // Add some sample transactions
        logActivity("Transfer completed: $500 from John Doe to Jane Smith");
        logActivity("Account created: Savings account for Charlie Wilson");
        logActivity("Loan application submitted: Bob Johnson - $15000 (Personal for 5 years)");
        logActivity("Savings goal created: Vacation Fund ($5000 target)");
        logActivity("Transfer completed: $200 from Alice Brown to John Doe");
    }

    public List<Customer> getAllCustomers() {
        return new ArrayList<>(customers.values());
    }

    public OperationResult<Customer> createCustomer(String name, String email, String phone) {
        if (name == null || name.isBlank()) {
            return OperationResult.failure("Customer name is required.");
        }
        if (email == null || email.isBlank() || !emailPattern.matcher(email).matches()) {
            return OperationResult.failure("A valid email address is required.");
        }
        if (phone == null || phone.isBlank()) {
            return OperationResult.failure("Customer phone number is required.");
        }
        if (!phone.matches("\\d{10}")) {
            return OperationResult.failure("Customer phone number must be exactly 10 digits.");
        }

        Customer customer = new Customer(name.trim(), email.trim(), phone.trim());
        customers.put(customer.getId(), customer);
        logActivity(String.format("Customer created: %s (%s)", customer.getName(), customer.getEmail()));
        return OperationResult.success("Customer created successfully.", customer);
    }

    public Customer getCustomer(int id) {
        return customers.get(id);
    }

    public OperationResult<Account> createAccount(int customerId, String accountType, double initialBalance) {
        if (accountType == null || accountType.isBlank()) {
            return OperationResult.failure("Please select an account type.");
        }
        if (initialBalance < 0) {
            return OperationResult.failure("Initial balance cannot be negative.");
        }

        Customer customer = customers.get(customerId);
        if (customer == null) {
            return OperationResult.failure("Customer not found.");
        }

        Account account = new Account(accountType.trim(), initialBalance);
        customer.addAccount(account);
        logActivity(String.format("Account created: #%d for %s", account.getAccountNumber(), customer.getName()));
        return OperationResult.success("Account created successfully.", account);
    }

    public OperationResult<Void> deposit(int customerId, int accountIndex, double amount) {
        if (amount <= 0) {
            return OperationResult.failure("Deposit amount must be greater than zero.");
        }
        Account account = getAccount(customerId, accountIndex);
        if (account == null) {
            return OperationResult.failure("Account not found.");
        }
        account.deposit(amount);
        logActivity(String.format("Deposit of $%.2f to account #%d", amount, account.getAccountNumber()));
        return OperationResult.success("Deposit completed successfully.");
    }

    public OperationResult<Void> withdraw(int customerId, int accountIndex, double amount) {
        if (amount <= 0) {
            return OperationResult.failure("Withdrawal amount must be greater than zero.");
        }
        Account account = getAccount(customerId, accountIndex);
        if (account == null) {
            return OperationResult.failure("Account not found.");
        }
        if (!account.withdraw(amount)) {
            return OperationResult.failure("Insufficient funds or invalid withdrawal amount.");
        }
        logActivity(String.format("Withdrawal of $%.2f from account #%d", amount, account.getAccountNumber()));
        return OperationResult.success("Withdrawal completed successfully.");
    }

    public OperationResult<Void> transfer(int customerId, int accountIndex, String targetAccountNumberStr, double amount) {
        if (amount <= 0) {
            return OperationResult.failure("Transfer amount must be greater than zero.");
        }
        
        // Validate that target account number is exactly 10 digits
        if (targetAccountNumberStr == null || !targetAccountNumberStr.matches("\\d{10}")) {
            return OperationResult.failure("Recipient account number must be exactly 10 digits.");
        }
        
        int targetAccountNumber;
        try {
            targetAccountNumber = Integer.parseInt(targetAccountNumberStr);
        } catch (NumberFormatException e) {
            return OperationResult.failure("Invalid account number format.");
        }
        
        Account sourceAccount = getAccount(customerId, accountIndex);
        if (sourceAccount == null) {
            return OperationResult.failure("Source account not found.");
        }
        Account targetAccount = findAccountByNumber(targetAccountNumber);
        if (targetAccount == null) {
            return OperationResult.failure("Target account number not found.");
        }
        if (sourceAccount.getAccountNumber() == targetAccountNumber) {
            return OperationResult.failure("You cannot transfer to the same account.");
        }
        if (!sourceAccount.transfer(targetAccount, amount)) {
            return OperationResult.failure("Insufficient funds or invalid transfer amount.");
        }
        logActivity(String.format("Transfer of $%.2f from account #%d to account #%d", amount, sourceAccount.getAccountNumber(), targetAccount.getAccountNumber()));
        return OperationResult.success("Transfer completed successfully.");
    }

    public int getTotalAccounts() {
        return customers.values().stream().mapToInt(c -> c.getAccounts().size()).sum();
    }

    public double getTotalBalance() {
        return customers.values().stream().flatMap(c -> c.getAccounts().stream()).mapToDouble(Account::getBalance).sum();
    }

    public int getTotalTransactions() {
        return customers.values().stream().flatMap(c -> c.getAccounts().stream()).mapToInt(acc -> acc.getTransactions().size()).sum();
    }

    public OperationResult<Void> applyLoan(String name, String email, String phone, double amount, double interestRate, int loanTerm, String loanType) {
        if (name == null || name.isBlank()) {
            return OperationResult.failure("Applicant name is required.");
        }
        if (email == null || email.isBlank() || !emailPattern.matcher(email).matches()) {
            return OperationResult.failure("A valid email address is required.");
        }
        if (phone == null || !phone.matches("\\d{10}")) {
            return OperationResult.failure("Phone number must be exactly 10 digits.");
        }
        if (amount <= 0) {
            return OperationResult.failure("Loan amount must be greater than zero.");
        }
        if (interestRate <= 0 || interestRate > 100) {
            return OperationResult.failure("A valid interest rate is required.");
        }
        if (loanTerm <= 0) {
            return OperationResult.failure("Loan term must be at least 1 year.");
        }
        if (loanType == null || loanType.isBlank()) {
            return OperationResult.failure("Loan type is required.");
        }
        logActivity(String.format("Loan application submitted: %s - $%.2f (%s for %d years)", name, amount, loanType, loanTerm));
        return OperationResult.success("Loan application submitted successfully. We will contact you soon.");
    }

    public OperationResult<SavingsGoal> createSavingsGoal(String name, String category, double targetAmount, double currentAmount, String targetDate) {
        if (name == null || name.isBlank()) {
            return OperationResult.failure("Goal name is required.");
        }
        if (category == null || category.isBlank()) {
            return OperationResult.failure("Goal category is required.");
        }
        if (targetDate == null || targetDate.isBlank()) {
            return OperationResult.failure("Target date is required.");
        }
        if (targetAmount <= 0) {
            return OperationResult.failure("Target amount must be greater than zero.");
        }
        if (currentAmount < 0) {
            return OperationResult.failure("Current savings cannot be negative.");
        }
        if (currentAmount > targetAmount) {
            return OperationResult.failure("Current savings cannot exceed the target amount.");
        }

        SavingsGoal goal = new SavingsGoal(name.trim(), category.trim(), targetAmount, currentAmount, targetDate);
        savingsGoals.add(goal);
        logActivity(String.format("Savings goal created: %s ($%.2f target)", goal.getName(), goal.getTargetAmount()));
        return OperationResult.success("Savings goal created successfully.", goal);
    }

    public List<SavingsGoal> getGoals() {
        return new ArrayList<>(savingsGoals);
    }

    public List<String> getRecentActivities() {
        return activityLog.stream().limit(5).collect(Collectors.toList());
    }

    private void logActivity(String activity) {
        activityLog.addFirst(activity);
        if (activityLog.size() > 10) {
            activityLog.removeLast();
        }
    }

    public Map<String, Long> getAccountTypeCounts() {
        return customers.values().stream()
                .flatMap(c -> c.getAccounts().stream())
                .collect(Collectors.groupingBy(Account::getAccountType, Collectors.counting()));
    }

    public Map<String, Double> getBalanceRanges() {
        Map<String, Double> ranges = new LinkedHashMap<>();

        // Get all account balances
        List<Double> balances = customers.values().stream()
                .flatMap(c -> c.getAccounts().stream())
                .map(Account::getBalance)
                .collect(Collectors.toList());

        if (balances.isEmpty()) {
            return ranges;
        }

        // Calculate distribution by balance ranges
        long lowBalance = balances.stream().filter(balance -> balance < 1000).count();
        long mediumBalance = balances.stream().filter(balance -> balance >= 1000 && balance < 10000).count();
        long highBalance = balances.stream().filter(balance -> balance >= 10000).count();

        long totalAccounts = balances.size();
        if (totalAccounts > 0) {
            ranges.put("Low (<$1K)", Math.round((double) lowBalance / totalAccounts * 100 * 10) / 10.0);
            ranges.put("Medium ($1K-$10K)", Math.round((double) mediumBalance / totalAccounts * 100 * 10) / 10.0);
            ranges.put("High (>$10K)", Math.round((double) highBalance / totalAccounts * 100 * 10) / 10.0);
        }

        return ranges;
    }

    public List<Map<String, Object>> getTopCustomersByBalance() {
        return customers.values().stream()
                .sorted((c1, c2) -> Double.compare(c2.getTotalBalance(), c1.getTotalBalance()))
                .limit(5)
                .map(customer -> {
                    Map<String, Object> data = new HashMap<>();
                    data.put("name", customer.getName());
                    data.put("balance", customer.getTotalBalance());
                    data.put("accounts", customer.getAccounts().size());
                    return data;
                })
                .collect(Collectors.toList());
    }

    public Map<String, Integer> getMonthlyTransactionTrend() {
        // More realistic transaction data for April 2026
        Map<String, Integer> trend = new LinkedHashMap<>();
        trend.put("Oct 2025", 42);
        trend.put("Nov 2025", 58);
        trend.put("Dec 2025", 73);
        trend.put("Jan 2026", 65);
        trend.put("Feb 2026", 89);
        trend.put("Mar 2026", 94);
        trend.put("Apr 2026", 78); // Current month
        return trend;
    }

    private Account getAccount(int customerId, int accountIndex) {
        Customer customer = customers.get(customerId);
        if (customer == null || accountIndex < 0 || accountIndex >= customer.getAccounts().size()) {
            return null;
        }
        return customer.getAccounts().get(accountIndex);
    }

    private Account findAccountByNumber(int accountNumber) {
        return customers.values().stream()
                .flatMap(c -> c.getAccounts().stream())
                .filter(acc -> acc.getAccountNumber() == accountNumber)
                .findFirst()
                .orElse(null);
    }
}
