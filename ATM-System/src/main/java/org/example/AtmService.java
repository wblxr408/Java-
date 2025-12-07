package org.example;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class AtmService {
    private final Account account;
    private final List<DepositAccount> termDeposits;

    public AtmService(Account account) {
        this.account = Objects.requireNonNull(account, "account");
        this.termDeposits = new ArrayList<>();
    }

    public Result deposit(BigDecimal amount) {
        if (amount == null) {
            return new Result(false, "Invalid amount. Use up to 2 decimals and > 0.");
        }
        return account.deposit(amount)
                ? new Result(true, "Deposited successfully.")
                : new Result(false, "Deposit failed.");
    }

    public Result withdraw(BigDecimal amount) {
        if (amount == null) {
            return new Result(false, "Invalid amount. Use up to 2 decimals and > 0.");
        }
        return account.withdraw(amount)
                ? new Result(true, "Withdrawn successfully.")
                : new Result(false, "Withdrawal failed. Insufficient funds or invalid amount.");
    }

    public Result createTermDeposit(BigDecimal amount, int termMonths) {
        if (amount == null || amount.signum() <= 0) {
            return new Result(false, "Invalid deposit amount.");
        }

        if (account.balance().compareTo(amount) < 0) {
            return new Result(false, "Insufficient balance for term deposit.");
        }

        try {
            if (!account.withdraw(amount)) {
                return new Result(false, "Failed to withdraw funds.");
            }

            DepositAccount deposit = new DepositAccount(account.owner(), amount, termMonths);
            termDeposits.add(deposit);

            return new Result(true, String.format("Term deposit created! %s", deposit));
        } catch (IllegalArgumentException e) {
            return new Result(false, e.getMessage());
        }
    }

    public List<DepositAccount> getActiveTermDeposits() {
        return termDeposits.stream().filter(d -> !d.isRedeemed()).toList();
    }

    public Result redeemTermDeposit(int index) {
        if (index < 0 || index >= termDeposits.size()) {
            return new Result(false, "Invalid deposit index.");
        }

        DepositAccount deposit = termDeposits.get(index);

        if (deposit.isRedeemed()) {
            return new Result(false, "This deposit has already been redeemed.");
        }

        if (!deposit.isMature()) {
            return new Result(false,
                    String.format("Deposit not yet mature. Needs %d more years.",
                            (int)Math.ceil(deposit.getTermMonths()/12.0) - deposit.getElapsedYears()));
        }

        account.deposit(deposit.getMaturityAmount());
        deposit.markRedeemed();

        return new Result(true,
                String.format("Redeemed! $%.2f credited to your account (Interest: $%.2f)",
                        deposit.getMaturityAmount(), deposit.getInterest()));
    }

    public BigDecimal balance() {
        return account.balance();
    }

    public synchronized int processYearPassed() {
        int maturedCount = 0;

        for (DepositAccount deposit : termDeposits) {
            if (!deposit.isRedeemed()) {
                boolean wasMature = deposit.isMature();
                deposit.addYear();

                if (!wasMature && deposit.isMature()) {
                    maturedCount++;
                }
            }
        }

        return maturedCount;
    }

    public synchronized BigDecimal getTotalValue() {
        BigDecimal total = account.balance();

        for (DepositAccount deposit : termDeposits) {
            if (!deposit.isRedeemed()) {
                total = total.add(deposit.getCurrentValue());
            }
        }

        return total;
    }
}