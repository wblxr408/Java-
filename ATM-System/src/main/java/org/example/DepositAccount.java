package org.example;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.Objects;

public final class DepositAccount {
    private final BigDecimal principal;
    private final int termMonths;
    private final LocalDate maturityDate;
    private final BigDecimal maturityAmount;
    private boolean redeemed;
    private int elapsedYears;

    public DepositAccount(String owner, BigDecimal principal, int termMonths) {
        String owner1 = Objects.requireNonNull(owner, "owner");
        if (principal == null || principal.signum() <= 0) {
            throw new IllegalArgumentException("Principal must be positive");
        }
        this.principal = principal;
        this.termMonths = termMonths;
        LocalDate startDate = LocalDate.now();
        this.maturityDate = startDate.plusMonths(termMonths);
        this.maturityAmount = InterestCalculator.calculate(principal, termMonths);
        this.redeemed = false;
        this.elapsedYears = 0;
    }

    public BigDecimal getPrincipal() {
        return principal;
    }

    public int getTermMonths() {
        return termMonths;
    }

    public LocalDate getMaturityDate() {
        return maturityDate;
    }

    public BigDecimal getMaturityAmount() {
        return maturityAmount;
    }

    public BigDecimal getInterest() {
        return maturityAmount.subtract(principal);
    }

    public boolean isRedeemed() {
        return redeemed;
    }

    public void markRedeemed() {
        this.redeemed = true;
    }

    public boolean isMature() {
        int termYears = (int) Math.ceil(termMonths / 12.0);
        return elapsedYears >= termYears;
    }

    public void addYear() {
        elapsedYears++;
    }

    public int getElapsedYears() {
        return elapsedYears;
    }

    public BigDecimal getCurrentValue() {
        if (elapsedYears >= Math.ceil(termMonths / 12.0)) {
            return maturityAmount;
        }
        BigDecimal totalInterest = maturityAmount.subtract(principal);
        BigDecimal termYears = new BigDecimal(termMonths).divide(new BigDecimal("12"), 4, RoundingMode.HALF_UP);
        BigDecimal elapsedYearsBD = new BigDecimal(elapsedYears);
        BigDecimal accruedInterest = totalInterest.multiply(elapsedYearsBD).divide(termYears, 2, RoundingMode.HALF_UP);
        return principal.add(accruedInterest);
    }

    @Override
    public String toString() {
        return String.format("Deposit: $%.2f for %d months (Rate: %s) | Years: %d/%d | Current: $%.2f → Maturity: $%.2f",
                principal, termMonths, InterestCalculator.getRateDisplay(termMonths),
                elapsedYears, (int) Math.ceil(termMonths / 12.0),
                getCurrentValue(), maturityAmount);
    }
}