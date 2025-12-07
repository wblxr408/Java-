package org.example;

import java.math.BigDecimal;

/**
 * Account abstraction.
 */
public interface Account {
    boolean deposit(BigDecimal amount);
    boolean withdraw(BigDecimal amount);
    BigDecimal balance();
    String owner();
}