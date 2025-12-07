import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.Assert;
import org.junit.Test;

public class BankTest {
    @Test
    public void depositAndWithdrawlObeyRules() throws Exception {
        Path storage = Files.createTempFile("bank-test", ".dat");
        Bank bank = new Bank(storage);
        User user = bank.registerUser("user1", "李四", User.Gender.MALE, "pwd");

        Assert.assertTrue(bank.deposit(user, 500));
        Assert.assertEquals(500.0, bank.queryBalance(user), 0.0001);
        Assert.assertFalse(bank.deposit(user, -1));
        Assert.assertFalse(bank.withdrawl(user, 600));
        Assert.assertTrue(bank.withdrawl(user, 200));
        Assert.assertEquals(300.0, bank.queryBalance(user), 0.0001);
    }

    @Test
    public void persistenceKeepsLatestBalances() throws Exception {
        Path storage = Files.createTempFile("bank-persist", ".dat");
        Bank bank = new Bank(storage);
        User user = bank.registerUser("persist", "王五", User.Gender.UNKNOWN, "secret");
        bank.deposit(user, 200);
        bank.saveUsers();

        Bank reloaded = new Bank(storage);
        reloaded.loadUsers();
        User restored = reloaded.getUser("persist");
        Assert.assertEquals(200.0, restored.getBalance(), 0.0001);
    }

    @Test
    public void applyInterestIncreasesBalances() {
        Bank bank = new Bank();
        User user = bank.registerUser("interest", "XiaoLi", User.Gender.UNKNOWN, "pw");
        bank.deposit(user, 1000);
        bank.applyInterestToAllUsers(0.02);
        Assert.assertEquals(1020.0, bank.queryBalance(user), 0.0001);
    }

    @Test
    public void shouldLoadDefaultAdminUser() throws Exception {
        Path storage = Files.createTempFile("bank-admin", ".dat");
        Bank bank = new Bank(storage);
        bank.loadUsers();
        User admin = bank.login("admin", "123456");
        Assert.assertTrue(admin != null);
        Assert.assertEquals("admin", admin.getUsername());
    }

    @Test
    public void shouldNotAllowDuplicateUsernames() {
        Bank bank = new Bank();
        User user1 = bank.registerUser("duplicate", "用户1", User.Gender.MALE, "pass1");
        User user2 = bank.registerUser("duplicate", "用户2", User.Gender.FEMALE, "pass2");
        Assert.assertTrue(user1 != null);
        Assert.assertTrue(user2 == null);
    }

    @Test
    public void shouldValidateLoginCredentials() {
        Bank bank = new Bank();
        bank.registerUser("testuser", "测试", User.Gender.UNKNOWN, "correctpass");
        Assert.assertTrue(bank.login("testuser", "correctpass") != null);
        Assert.assertTrue(bank.login("testuser", "wrongpass") == null);
        Assert.assertTrue(bank.login("nonexistent", "anypass") == null);
    }

    @Test
    public void shouldChangePasswordCorrectly() {
        Bank bank = new Bank();
        User user = bank.registerUser("pwdtest", "密码测试", User.Gender.MALE, "old123");
        Assert.assertTrue(bank.changePassword(user, "old123", "new456"));
        Assert.assertTrue(bank.login("pwdtest", "old123") == null);
        Assert.assertTrue(bank.login("pwdtest", "new456") != null);
    }

    @Test
    public void shouldRejectInvalidPasswordChange() {
        Bank bank = new Bank();
        User user = bank.registerUser("pwdtest2", "测试2", User.Gender.FEMALE, "pass");
        Assert.assertFalse(bank.changePassword(user, "wrongold", "newpass"));
        Assert.assertFalse(bank.changePassword(user, "pass", ""));
        Assert.assertFalse(bank.changePassword(user, "pass", null));
    }

    @Test
    public void interestRateCanBeConfigured() {
        Bank bank = new Bank();
        bank.setInterestRate(0.05);
        Assert.assertEquals(0.05, bank.getInterestRate(), 0.0001);
    }

    @Test
    public void interestThreadCalculatesAutomatically() throws Exception {
        Bank bank = new Bank();
        User user = bank.registerUser("autointerest", "自动利息", User.Gender.UNKNOWN, "pass");
        bank.deposit(user, 1000);
        bank.setInterestRate(0.01);
        bank.startInterestCalculation(100);
        Thread.sleep(250);
        bank.stopInterestCalculation();
        double balance = bank.queryBalance(user);
        Assert.assertTrue(balance > 1000.0);
    }

    @Test
    public void shouldHandleZeroAndNegativeDeposits() {
        Bank bank = new Bank();
        User user = bank.registerUser("zerotest", "零测试", User.Gender.MALE, "pass");
        Assert.assertFalse(bank.deposit(user, 0));
        Assert.assertFalse(bank.deposit(user, -100));
        Assert.assertEquals(0.0, bank.queryBalance(user), 0.0001);
    }

    @Test
    public void shouldHandleZeroAndNegativeWithdrawals() {
        Bank bank = new Bank();
        User user = bank.registerUser("withdrawtest", "取款测试", User.Gender.FEMALE, "pass");
        bank.deposit(user, 500);
        Assert.assertFalse(bank.withdrawl(user, 0));
        Assert.assertFalse(bank.withdrawl(user, -50));
        Assert.assertEquals(500.0, bank.queryBalance(user), 0.0001);
    }
}
