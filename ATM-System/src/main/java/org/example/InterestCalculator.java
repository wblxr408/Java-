package org.example;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Interest calculator for term deposits.
 * Supports different terms with corresponding interest rates.
 */
public final class InterestCalculator {

    /**
     * Calculate interest based on principal, term (months), and annual rate.
     *
     * @param principal The initial deposit amount
     * @param months Term length in months (3, 6, 12, 24, 36)
     * @return Total amount after interest (principal + interest)
     */
    public static BigDecimal calculate(BigDecimal principal, int months) {
        if (principal == null || principal.signum() <= 0) {
            throw new IllegalArgumentException("Principal must be positive");
        }

        BigDecimal annualRate = getAnnualRate(months);

        // Simple interest: Interest = Principal × Rate × (Months / 12)
        BigDecimal years = new BigDecimal(months).divide(new BigDecimal("12"), 4, RoundingMode.HALF_UP);
        BigDecimal interest = principal.multiply(annualRate).multiply(years);

        return principal.add(interest).setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * Get annual interest rate based on term length.
     * Longer terms = higher rates (common banking practice)
     */
    private static BigDecimal getAnnualRate(int months) {
        return switch (months) {
            case 3 -> new BigDecimal("0.015");   // 1.5% annual rate
            case 6 -> new BigDecimal("0.020");   // 2.0% annual rate
            case 12 -> new BigDecimal("0.025");  // 2.5% annual rate
            case 24 -> new BigDecimal("0.030");  // 3.0% annual rate
            case 36 -> new BigDecimal("0.035");  // 3.5% annual rate
            default -> throw new IllegalArgumentException("Invalid term. Choose: 3, 6, 12, 24, or 36 months");
        };
    }

    /**
     * Get the annual rate as percentage string for display.
     */
    public static String getRateDisplay(int months) {
        BigDecimal rate = getAnnualRate(months);
        return rate.multiply(new BigDecimal("100")).setScale(2, RoundingMode.HALF_UP) + "%";
    }
}