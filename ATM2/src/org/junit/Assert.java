package org.junit;

public final class Assert {
    private Assert() {
    }

    public static void assertTrue(boolean condition) {
        if (!condition) {
            fail("Expected condition to be true but was false");
        }
    }

    public static void assertFalse(boolean condition) {
        if (condition) {
            fail("Expected condition to be false but was true");
        }
    }

    public static void assertEquals(int expected, int actual) {
        if (expected != actual) {
            fail("Expected " + expected + " but was " + actual);
        }
    }

    public static void assertEquals(Object expected, Object actual) {
        if (expected == null && actual == null) {
            return;
        }
        if (expected != null && expected.equals(actual)) {
            return;
        }
        fail("Expected " + expected + " but was " + actual);
    }

    public static void assertEquals(double expected, double actual, double delta) {
        if (Double.isInfinite(expected) || Double.isNaN(expected)) {
            if (expected == actual) {
                return;
            }
            fail("Expected " + expected + " but was " + actual);
        }
        if (Math.abs(expected - actual) <= delta) {
            return;
        }
        fail("Expected " + expected + " but was " + actual);
    }

    public static void fail(String message) {
        throw new AssertionError(message);
    }

    public static <T extends Throwable> T assertThrows(Class<T> expected, Runnable runnable) {
        try {
            runnable.run();
        } catch (Throwable throwable) {
            if (expected.isInstance(throwable)) {
                return expected.cast(throwable);
            }
            throw new AssertionError("Unexpected exception type: " + throwable.getClass());
        }
        throw new AssertionError("Expected exception: " + expected.getName());
    }
}
