package org.example;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Scanner;

public class ConsoleAtm {
    private final AtmService atmService;
    private final Scanner scanner;
    private final TimeSimulator timeSimulator;
    private final Thread simulatorThread;
    private final BigDecimal initialBalance;

    public ConsoleAtm() {
        this.initialBalance = new BigDecimal("1000.00");
        this.atmService = new AtmService(new SimpleAccount("User", initialBalance));
        this.scanner = new Scanner(System.in);
        this.timeSimulator = new TimeSimulator(atmService);
        this.simulatorThread = new Thread(timeSimulator);
        this.simulatorThread.setDaemon(true);
    }

    public static void main(String[] args) {
        ConsoleAtm atm = new ConsoleAtm();
        atm.run();
    }

    private void clearScreen() {
        System.out.println("\n\n\n");
        System.out.println("=".repeat(60));
    }

    public void run() {
        displayWelcomeMessage();

        System.out.print("Press Enter to start...");
        scanner.nextLine();

        simulatorThread.start();

        while (true) {
            displayMenu();
            int choice = getUserChoice();
            scanner.nextLine();

            if (choice == 0) {
                timeSimulator.stop();
                try {
                    simulatorThread.join(1000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                clearScreen();
                System.out.println("Thank you for using our ATM. Goodbye!");
                break;
            }

            processUserChoice(choice);
        }

        scanner.close();
    }

    private void displayWelcomeMessage() {
        System.out.println("\n");
        System.out.println("========================================");
        System.out.println("   WELCOME TO SIMPLE ATM SYSTEM");
        System.out.println("========================================");
        System.out.println();
        System.out.println("  Initial Balance: $" + initialBalance);
        System.out.println("  Time Simulation: Every 5 seconds = 1 year");
        System.out.println("  Your balance will grow with interest automatically!");
        System.out.println();
    }

    private void displayMenu() {
        clearScreen();

        BigDecimal currentBalance = atmService.balance();
        BigDecimal totalPortfolio = atmService.getTotalValue();
        int years = timeSimulator.getSimulatedYears();

        System.out.println("========================================");
        System.out.println("   SIMPLE ATM SYSTEM - DASHBOARD");
        System.out.println("========================================");
        System.out.println("Initial Balance:     $" + initialBalance);
        System.out.println("Simulated Year:      " + years + " (Real time: " + (years * 5) + "s)");
        System.out.println("----------------------------------------");
        System.out.println("Checking Balance:    $" + currentBalance.setScale(2, RoundingMode.DOWN));
        System.out.println("Total Portfolio:     $" + totalPortfolio.setScale(2, RoundingMode.DOWN));

        BigDecimal totalGain = totalPortfolio.subtract(initialBalance);
        if (totalGain.compareTo(BigDecimal.ZERO) > 0) {
            System.out.println("Total Gain:          +$" + totalGain.setScale(2, RoundingMode.DOWN) + " (UP)");
        } else if (totalGain.compareTo(BigDecimal.ZERO) < 0) {
            System.out.println("Total Loss:          $" + totalGain.setScale(2, RoundingMode.DOWN) + " (DOWN)");
        } else {
            System.out.println("Total Gain:          $0.00");
        }

        System.out.println("========================================");
        System.out.println("MENU OPTIONS:");
        System.out.println("  1: Deposit");
        System.out.println("  2: Withdrawal");
        System.out.println("  3: Query Balance");
        System.out.println("  4: Create Term Deposit");
        System.out.println("  5: View Term Deposits");
        System.out.println("  6: Redeem Matured Deposit");
        System.out.println("  0: Exit");
        System.out.println("========================================");
        System.out.print("Enter your choice: ");
    }

    private int getUserChoice() {
        while (true) {
            try {
                if (scanner.hasNextInt()) {
                    return scanner.nextInt();
                } else {
                    System.out.print("Invalid input. Enter a number: ");
                    scanner.next();
                }
            } catch (Exception e) {
                System.out.print("Input error. Try again: ");
                if (scanner.hasNext()) {
                    scanner.next();
                }
            }
        }
    }

    private void processUserChoice(int choice) {
        switch (choice) {
            case 1 -> handleDeposit();
            case 2 -> handleWithdrawal();
            case 3 -> handleQueryBalance();
            case 4 -> handleCreateTermDeposit();
            case 5 -> handleViewTermDeposits();
            case 6 -> handleRedeemDeposit();
            default -> System.out.println("Invalid choice. Please select 0-6.");
        }

        if (choice >= 1 && choice <= 6) {
            System.out.print("\nPress Enter to continue...");
            scanner.nextLine();
        }
    }

    private void handleDeposit() {
        System.out.print("Enter amount to deposit: $");
        BigDecimal amount = getAmountFromUser();

        if (amount != null) {
            Result result = atmService.deposit(amount);
            System.out.println(result.message);
            if (result.ok) {
                System.out.println("New balance: $" + atmService.balance().setScale(2, RoundingMode.DOWN));
            }
        } else {
            System.out.println("Invalid amount.");
        }
    }

    private void handleWithdrawal() {
        System.out.print("Enter amount to withdraw: $");
        BigDecimal amount = getAmountFromUser();

        if (amount != null) {
            Result result = atmService.withdraw(amount);
            System.out.println(result.message);
            if (result.ok) {
                System.out.println("New balance: $" + atmService.balance().setScale(2, RoundingMode.DOWN));
            }
        } else {
            System.out.println("Invalid amount.");
        }
    }

    private void handleQueryBalance() {
        System.out.println("\n=== Account Summary ===");
        System.out.println("Checking Balance: $" + atmService.balance().setScale(2, RoundingMode.DOWN));

        var deposits = atmService.getActiveTermDeposits();
        if (!deposits.isEmpty()) {
            BigDecimal totalInDeposits = BigDecimal.ZERO;
            for (DepositAccount d : deposits) {
                totalInDeposits = totalInDeposits.add(d.getCurrentValue());
            }
            System.out.println("Term Deposits Value: $" + totalInDeposits.setScale(2, RoundingMode.DOWN));
        }

        System.out.println("Total Portfolio: $" + atmService.getTotalValue().setScale(2, RoundingMode.DOWN));
        System.out.println("Simulated Years: " + timeSimulator.getSimulatedYears());
    }

    private void handleCreateTermDeposit() {
        System.out.println("\n=== Create Term Deposit ===");
        System.out.println("Available Terms & Rates:");
        System.out.println("  3 months  - " + InterestCalculator.getRateDisplay(3));
        System.out.println("  6 months  - " + InterestCalculator.getRateDisplay(6));
        System.out.println("  12 months - " + InterestCalculator.getRateDisplay(12));
        System.out.println("  24 months - " + InterestCalculator.getRateDisplay(24));
        System.out.println("  36 months - " + InterestCalculator.getRateDisplay(36));
        System.out.println();

        System.out.print("Enter deposit amount: $");
        BigDecimal amount = getAmountFromUser();
        if (amount == null) {
            System.out.println("Invalid amount.");
            return;
        }

        System.out.print("Enter term (3/6/12/24/36 months): ");
        int term = getUserChoice();

        Result result = atmService.createTermDeposit(amount, term);
        System.out.println(result.message);
        if (result.ok) {
            System.out.println("New checking balance: $" + atmService.balance().setScale(2, RoundingMode.DOWN));
        }
    }

    private void handleViewTermDeposits() {
        List<DepositAccount> deposits = atmService.getActiveTermDeposits();

        if (deposits.isEmpty()) {
            System.out.println("No active term deposits.");
            return;
        }

        System.out.println("\n=== Your Term Deposits ===");
        System.out.println("Simulated Time: Year " + timeSimulator.getSimulatedYears());
        System.out.println();

        for (int i = 0; i < deposits.size(); i++) {
            DepositAccount d = deposits.get(i);
            System.out.println("[" + i + "] " + d.toString());
            System.out.println("    Status: " +
                    (d.isMature() ? "MATURE - Ready to redeem!" :
                            "In progress (" + d.getElapsedYears() + "/" + (int)Math.ceil(d.getTermMonths()/12.0) + " years)"));
        }
    }

    private void handleRedeemDeposit() {
        List<DepositAccount> deposits = atmService.getActiveTermDeposits();

        if (deposits.isEmpty()) {
            System.out.println("No active term deposits to redeem.");
            return;
        }

        handleViewTermDeposits();
        System.out.print("\nEnter deposit number to redeem: ");
        int index = getUserChoice();

        Result result = atmService.redeemTermDeposit(index);
        System.out.println(result.message);
        if (result.ok) {
            System.out.println("New balance: $" + atmService.balance().setScale(2, RoundingMode.DOWN));
        }
    }

    private BigDecimal getAmountFromUser() {
        while (!scanner.hasNextDouble()) {
            System.out.print("Invalid input. Enter amount: $");
            scanner.next();
        }

        double input = scanner.nextDouble();
        if (input <= 0) return null;

        try {
            return new BigDecimal(String.valueOf(input)).setScale(2, RoundingMode.DOWN);
        } catch (NumberFormatException e) {
            return null;
        }
    }
}