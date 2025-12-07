package org.example;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

/**
 * Simple in-memory account implementation.
 */
public final class SimpleAccount implements Account {
    private final String nm;
    private BigDecimal bal;

    public SimpleAccount(String owner, BigDecimal initial) {
        String o = Objects.requireNonNull(owner, "owner").trim();
        if (o.isEmpty()) throw new IllegalArgumentException("owner blank");
        this.nm = o;
        BigDecimal init = initial == null ? BigDecimal.ZERO : initial.setScale(2, RoundingMode.DOWN);
        if (init.signum() < 0) throw new IllegalArgumentException("negative initial");
        this.bal = init;
    }

    @Override
    public synchronized boolean deposit(BigDecimal amount) {
        if (amount == null || amount.signum() <= 0) return false;
        bal = bal.add(amount);
        return true;
    }

    @Override
    public synchronized boolean withdraw(BigDecimal amount) {
        if (amount == null || amount.signum() <= 0) return false;
        if (bal.compareTo(amount) < 0) return false;
        bal = bal.subtract(amount);
        return true;
    }

    @Override
    public synchronized BigDecimal balance() {
        return bal;
    }

    @Override
    public String owner() {
        return nm;
    }
}