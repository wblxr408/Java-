import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.junit.Test;

public class TestRunner {
    public static void main(String[] args) throws Exception {
        Class<?>[] testClasses = {UserTest.class, BankTest.class, ATMTest.class};
        int passed = 0;
        int failed = 0;
        for (Class<?> testClass : testClasses) {
            Object instance = testClass.getDeclaredConstructor().newInstance();
            for (Method method : testClass.getDeclaredMethods()) {
                if (!method.isAnnotationPresent(Test.class)) {
                    continue;
                }
                try {
                    method.invoke(instance);
                    passed++;
                    System.out.println("[PASS] " + testClass.getSimpleName() + "." + method.getName());
                } catch (InvocationTargetException ex) {
                    failed++;
                    Throwable cause = ex.getCause() == null ? ex : ex.getCause();
                    System.out.println("[FAIL] " + testClass.getSimpleName() + "." + method.getName() + " -> " + cause);
                }
            }
        }
        System.out.println("Passed: " + passed + ", Failed: " + failed);
        if (failed > 0) {
            throw new AssertionError("Tests failed");
        }
    }
}
