package org.example;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Time simulator that runs in background thread.
 * Every 5 seconds = 1 year in simulation.
 * Automatically calculates and applies interest to term deposits.
 */
public class TimeSimulator implements Runnable {
    private final AtmService atmService;
    private volatile boolean running;
    private int simulatedYears;
    private static final int SECONDS_PER_YEAR = 5;

    public TimeSimulator(AtmService atmService) {
        this.atmService = atmService;
        this.running = true;
        this.simulatedYears = 0;
    }

    @Override
    public void run() {
        System.out.println("\n⏰ Time Simulator Started (5 seconds = 1 year)\n");

        while (running) {
            try {
                Thread.sleep(SECONDS_PER_YEAR * 1000); // 5 seconds

                if (!running) break;

                simulatedYears++;
                processYearPassed();

            } catch (InterruptedException e) {
                break;
            }
        }

        System.out.println("\n⏰ Time Simulator Stopped\n");
    }

    private void processYearPassed() {
        // Apply interest to all active term deposits
        int maturedCount = atmService.processYearPassed();

        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));

        System.out.println("\n" + "=".repeat(50));
        System.out.println("⏰ [" + timestamp + "] Year " + simulatedYears + " passed!");
        System.out.println("   Current Balance: $" + atmService.balance().setScale(2, RoundingMode.DOWN));

        if (maturedCount > 0) {
            System.out.println("   🎉 " + maturedCount + " deposit(s) matured!");
        }

        // Show all active deposits status
        var deposits = atmService.getActiveTermDeposits();
        if (!deposits.isEmpty()) {
            System.out.println("   Active Term Deposits: " + deposits.size());
            for (int i = 0; i < deposits.size(); i++) {
                DepositAccount d = deposits.get(i);
                int yearsLeft = (int) Math.ceil(d.getTermMonths() / 12.0 - simulatedYears);
                if (yearsLeft > 0) {
                    System.out.println("     [" + i + "] $" + d.getPrincipal() +
                            " - " + yearsLeft + " years until maturity");
                } else {
                    System.out.println("     [" + i + "] $" + d.getPrincipal() +
                            " - ✓ MATURED (Total: $" + d.getMaturityAmount() + ")");
                }
            }
        }

        System.out.println("=".repeat(50));
        System.out.print("Enter your choice: ");
    }

    public void stop() {
        running = false;
    }

    public int getSimulatedYears() {
        return simulatedYears;
    }
}