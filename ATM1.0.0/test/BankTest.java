import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BankTest {

    @Test
    void createsAuthenticatesAndPersistsUsers() throws IOException {
        Path storage = createTempStorage();
        Bank bank = new Bank(storage);

        User newUser = bank.createUser("user1", "张三", "男", "pwd123");
        bank.deposit(newUser, 500);
        assertEquals(500, bank.queryBalance(newUser));

        assertTrue(bank.authenticate("user1", "pwd123").isPresent());
        assertThrows(IllegalArgumentException.class, () -> bank.withdraw(newUser, 600));

        bank.withdraw(newUser, 200);
        assertEquals(300, bank.queryBalance(newUser));
        bank.applyInterestToAllUsers();
        assertTrue(bank.queryBalance(newUser) >= 300);
        bank.saveUsers();

        Bank reloaded = new Bank(storage);
        assertTrue(reloaded.authenticate("user1", "pwd123").isPresent());
        assertEquals(300, reloaded.queryBalance(newUser));
    }

    @Test
    void validatesDepositAndPasswordChange() throws IOException {
        Path storage = createTempStorage();
        Bank bank = new Bank(storage);
        User user = bank.createUser("user2", "李四", "女", "123456");

        assertThrows(IllegalArgumentException.class, () -> bank.deposit(user, -1));
        assertThrows(IllegalArgumentException.class, () -> bank.withdraw(user, 1));

        bank.deposit(user, 100);
        assertEquals(100, bank.queryBalance(user));
        bank.applyInterestToAllUsers();
        assertTrue(bank.queryBalance(user) >= 100);

        assertThrows(IllegalArgumentException.class, () -> bank.changePassword(user, "wrong", "new"));
        bank.changePassword(user, "123456", "newPass");
        assertTrue(bank.authenticate("user2", "newPass").isPresent());
    }

    private Path createTempStorage() throws IOException {
        Path storage = Files.createTempFile("bank-test", ".dat");
        Files.deleteIfExists(storage);
        return storage;
    }
}
