package com.bank.app;

import com.bank.app.model.Account;
import com.bank.app.model.Customer;
import com.bank.app.model.OperationResult;
import com.bank.app.model.SavingsGoal;
import com.bank.app.service.BankService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.util.List;

@Controller
public class BankController {

    private final BankService bankService;

    public BankController(BankService bankService) {
        this.bankService = bankService;
    }

    @GetMapping("/")
    public String home() {
        return "index";
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        model.addAttribute("totalCustomers", bankService.getAllCustomers().size());
        model.addAttribute("totalAccounts", bankService.getTotalAccounts());
        model.addAttribute("totalBalance", bankService.getTotalBalance());
        model.addAttribute("totalTransactions", bankService.getTotalTransactions());
        model.addAttribute("customers", bankService.getAllCustomers());
        model.addAttribute("recentActivities", bankService.getRecentActivities());
        
        // Analytics data
        model.addAttribute("accountTypeCounts", bankService.getAccountTypeCounts());
        model.addAttribute("balanceRanges", bankService.getBalanceRanges());
        model.addAttribute("topCustomers", bankService.getTopCustomersByBalance());
        model.addAttribute("monthlyTrends", bankService.getMonthlyTransactionTrend());
        
        return "dashboard";
    }

    @GetMapping("/customers")
    public String customers(Model model) {
        model.addAttribute("customers", bankService.getAllCustomers());
        return "customers";
    }

    @PostMapping("/createCustomer")
    public String createCustomer(@RequestParam String name,
                                 @RequestParam String email,
                                 @RequestParam String phone,
                                 RedirectAttributes redirectAttributes) {
        OperationResult<Customer> result = bankService.createCustomer(name, email, phone);
        if (result.isSuccess()) {
            redirectAttributes.addFlashAttribute("successMessage", result.getMessage());
        } else {
            redirectAttributes.addFlashAttribute("errorMessage", result.getMessage());
        }
        return "redirect:/dashboard";
    }

    @GetMapping("/customer/{id}")
    public String viewCustomer(@PathVariable int id, Model model) {
        Customer customer = bankService.getCustomer(id);
        if (customer == null) {
            return "redirect:/dashboard";
        }
        model.addAttribute("customer", customer);
        return "customer-details";
    }

    @PostMapping("/createAccount/{customerId}")
    public String createAccount(@PathVariable int customerId,
                                @RequestParam String accountType,
                                @RequestParam double initialBalance,
                                RedirectAttributes redirectAttributes) {
        OperationResult<Account> result = bankService.createAccount(customerId, accountType, initialBalance);
        if (result.isSuccess()) {
            redirectAttributes.addFlashAttribute("successMessage", result.getMessage());
        } else {
            redirectAttributes.addFlashAttribute("errorMessage", result.getMessage());
        }
        return "redirect:/customer/" + customerId;
    }

    @PostMapping("/createAccountFromDashboard")
    public String createAccountFromDashboard(@RequestParam int customerId,
                                             @RequestParam String accountType,
                                             @RequestParam double initialBalance,
                                             RedirectAttributes redirectAttributes) {
        OperationResult<Account> result = bankService.createAccount(customerId, accountType, initialBalance);
        if (result.isSuccess()) {
            redirectAttributes.addFlashAttribute("successMessage", result.getMessage());
        } else {
            redirectAttributes.addFlashAttribute("errorMessage", result.getMessage());
        }
        return "redirect:/dashboard";
    }

    @GetMapping("/account/{customerId}/{accountIndex}")
    public String viewAccountTransactions(@PathVariable int customerId,
                                          @PathVariable int accountIndex,
                                          Model model) {
        Customer customer = bankService.getCustomer(customerId);
        if (customer != null && accountIndex >= 0 && accountIndex < customer.getAccounts().size()) {
            Account account = customer.getAccounts().get(accountIndex);
            model.addAttribute("customer", customer);
            model.addAttribute("account", account);
            model.addAttribute("accountIndex", accountIndex);
            return "account-details";
        }
        return "redirect:/dashboard";
    }

    @PostMapping("/deposit/{customerId}/{accountIndex}")
    public String deposit(@PathVariable int customerId,
                          @PathVariable int accountIndex,
                          @RequestParam double amount,
                          RedirectAttributes redirectAttributes) {
        OperationResult<Void> result = bankService.deposit(customerId, accountIndex, amount);
        if (result.isSuccess()) {
            redirectAttributes.addFlashAttribute("successMessage", result.getMessage());
        } else {
            redirectAttributes.addFlashAttribute("errorMessage", result.getMessage());
        }
        return "redirect:/account/" + customerId + "/" + accountIndex;
    }

    @PostMapping("/withdraw/{customerId}/{accountIndex}")
    public String withdraw(@PathVariable int customerId,
                           @PathVariable int accountIndex,
                           @RequestParam double amount,
                           RedirectAttributes redirectAttributes) {
        OperationResult<Void> result = bankService.withdraw(customerId, accountIndex, amount);
        if (result.isSuccess()) {
            redirectAttributes.addFlashAttribute("successMessage", result.getMessage());
        } else {
            redirectAttributes.addFlashAttribute("errorMessage", result.getMessage());
        }
        return "redirect:/account/" + customerId + "/" + accountIndex;
    }

    @GetMapping("/transfer/{customerId}/{accountIndex}")
    public String transferPage(@PathVariable int customerId,
                               @PathVariable int accountIndex,
                               Model model) {
        Customer customer = bankService.getCustomer(customerId);
        if (customer != null && accountIndex >= 0 && accountIndex < customer.getAccounts().size()) {
            model.addAttribute("customer", customer);
            model.addAttribute("accountIndex", accountIndex);
            model.addAttribute("sourceAccount", customer.getAccounts().get(accountIndex));
            return "transfer";
        }
        return "redirect:/dashboard";
    }

    @PostMapping("/transfer/{customerId}/{accountIndex}")
    public String executeTransfer(@PathVariable int customerId,
                                  @PathVariable int accountIndex,
                                  @RequestParam String targetAccountNumber,
                                  @RequestParam double amount,
                                  RedirectAttributes redirectAttributes) {
        OperationResult<Void> result = bankService.transfer(customerId, accountIndex, targetAccountNumber, amount);
        if (result.isSuccess()) {
            redirectAttributes.addFlashAttribute("successMessage", result.getMessage());
        } else {
            redirectAttributes.addFlashAttribute("errorMessage", result.getMessage());
        }
        return "redirect:/transfer/" + customerId + "/" + accountIndex;
    }

    @GetMapping("/analytics")
    public String analytics(Model model) {
        model.addAttribute("totalCustomers", bankService.getAllCustomers().size());
        model.addAttribute("totalAccounts", bankService.getTotalAccounts());
        model.addAttribute("totalBalance", bankService.getTotalBalance());
        model.addAttribute("totalTransactions", bankService.getTotalTransactions());
        return "analytics";
    }

    @GetMapping("/loan-calculator")
    public String loanCalculator(Model model) {
        model.addAttribute("loanTypes", List.of("Home Loan", "Auto Loan", "Personal Loan", "Business Loan"));
        return "loan-calculator";
    }

    @PostMapping("/applyLoan")
    public String applyLoan(@RequestParam String name,
                            @RequestParam String email,
                            @RequestParam String phone,
                            @RequestParam double loanAmount,
                            @RequestParam double interestRate,
                            @RequestParam int loanTerm,
                            @RequestParam String loanType,
                            RedirectAttributes redirectAttributes) {
        OperationResult<Void> result = bankService.applyLoan(name, email, phone, loanAmount, interestRate, loanTerm, loanType);
        if (result.isSuccess()) {
            redirectAttributes.addFlashAttribute("successMessage", result.getMessage());
        } else {
            redirectAttributes.addFlashAttribute("errorMessage", result.getMessage());
        }
        return "redirect:/loan-calculator";
    }

    @GetMapping("/savings-goals")
    public String savingsGoals(Model model) {
        model.addAttribute("goals", bankService.getGoals());
        return "savings-goals";
    }

    @PostMapping("/createGoal")
    public String createGoal(@RequestParam String goalName,
                             @RequestParam String goalCategory,
                             @RequestParam double targetAmount,
                             @RequestParam String targetDate,
                             @RequestParam(required = false, defaultValue = "0") double currentAmount,
                             RedirectAttributes redirectAttributes) {
        OperationResult<SavingsGoal> result = bankService.createSavingsGoal(goalName, goalCategory, targetAmount, currentAmount, targetDate);
        if (result.isSuccess()) {
            redirectAttributes.addFlashAttribute("successMessage", result.getMessage());
        } else {
            redirectAttributes.addFlashAttribute("errorMessage", result.getMessage());
        }
        return "redirect:/savings-goals";
    }
}