package org.example;

/**
 * Outcome container for service operations.
 */
public final class Result {
    public final boolean ok;
    public final String message;

    public Result(boolean ok, String message) {
        this.ok = ok;
        this.message = message;
    }
}