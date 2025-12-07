import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UserTest {

    @Test
    void storesBasicInformationAndBalance() {
        LocalDateTime now = LocalDateTime.now();
        User user = new User("admin", "管理员", "男", "123456", now);

        assertEquals("admin", user.getUsername());
        assertEquals("管理员", user.getName());
        assertEquals("男", user.getGender());
        assertEquals(now, user.getCreatedAt());
        assertEquals(0, user.getBalance());
    }

    @Test
    void updatesPasswordAndBalance() {
        User user = new User("test", "测试", "女", "initial", LocalDateTime.now());

        assertTrue(user.passwordMatches("initial"));
        assertFalse(user.passwordMatches("other"));

        user.updatePassword("newPassword");
        assertTrue(user.passwordMatches("newPassword"));

        user.credit(300);
        assertEquals(300, user.getBalance());
        user.debit(100);
        assertEquals(200, user.getBalance());
        int delta = user.applyInterest(0.05d);
        assertTrue(delta >= 0);
        assertEquals(200 + delta, user.getBalance());
    }
}
