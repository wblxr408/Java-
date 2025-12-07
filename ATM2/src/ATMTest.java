import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.Assert;
import org.junit.Test;

public class ATMTest {
    @Test
    public void loginQueryAndLogoutFlow() throws Exception {
        Path storage = Files.createTempFile("atm-test", ".dat");
        Bank bank = new Bank(storage);
        User user = bank.registerUser("tester", "赵六", User.Gender.FEMALE, "pass");
        bank.deposit(user, 100);
        bank.saveUsers();

        String commands = String.join(System.lineSeparator(),
                "1",
                "tester",
                "pass",
                "3",
                "5",
                "3") + System.lineSeparator();
        ByteArrayInputStream input = new ByteArrayInputStream(commands.getBytes(StandardCharsets.UTF_8));
        ByteArrayOutputStream outputBytes = new ByteArrayOutputStream();
        PrintStream output = new PrintStream(outputBytes, true, "UTF-8");

        ATM atm = new ATM(bank, input, output);
        atm.setEnableInterestCalculation(false);
        atm.start();

        String console = outputBytes.toString("UTF-8");
        Assert.assertTrue(console.contains("登录成功"));
        Assert.assertTrue(console.contains("当前余额: 100.00"));
        Assert.assertTrue(console.contains("用户 tester 已退出"));
    }

    @Test
    public void registrationFlow() throws Exception {
        Path storage = Files.createTempFile("atm-register", ".dat");
        Bank bank = new Bank(storage);

        String commands = String.join(System.lineSeparator(),
                "2",
                "newuser",
                "TestUser",
                "M",
                "password123",
                "3") + System.lineSeparator();
        ByteArrayInputStream input = new ByteArrayInputStream(commands.getBytes(StandardCharsets.UTF_8));
        ByteArrayOutputStream outputBytes = new ByteArrayOutputStream();
        PrintStream output = new PrintStream(outputBytes, true, "UTF-8");

        ATM atm = new ATM(bank, input, output);
        atm.setEnableInterestCalculation(false);
        atm.start();

        String console = outputBytes.toString("UTF-8");
        Assert.assertTrue(console.contains("用户创建成功"));
        User registered = bank.getUser("newuser");
        Assert.assertTrue(registered != null);
        Assert.assertEquals("TestUser", registered.getName());
    }

    @Test
    public void depositFlow() throws Exception {
        Path storage = Files.createTempFile("atm-deposit", ".dat");
        Bank bank = new Bank(storage);
        bank.registerUser("depositor", "Depositor", User.Gender.MALE, "pass");

        String commands = String.join(System.lineSeparator(),
                "1",
                "depositor",
                "pass",
                "1",
                "500",
                "0") + System.lineSeparator();
        ByteArrayInputStream input = new ByteArrayInputStream(commands.getBytes(StandardCharsets.UTF_8));
        ByteArrayOutputStream outputBytes = new ByteArrayOutputStream();
        PrintStream output = new PrintStream(outputBytes, true, "UTF-8");

        ATM atm = new ATM(bank, input, output);
        atm.setEnableInterestCalculation(false);
        atm.start();

        String console = outputBytes.toString("UTF-8");
        Assert.assertTrue(console.contains("存款成功"));
        double balance = bank.queryBalance(bank.getUser("depositor"));
        Assert.assertTrue(balance >= 500.0);
    }

    @Test
    public void withdrawalFlow() throws Exception {
        Path storage = Files.createTempFile("atm-withdraw", ".dat");
        Bank bank = new Bank(storage);
        User user = bank.registerUser("withdrawer", "Withdrawer", User.Gender.FEMALE, "pass");
        bank.deposit(user, 1000);

        String commands = String.join(System.lineSeparator(),
                "1",
                "withdrawer",
                "pass",
                "2",
                "300",
                "0") + System.lineSeparator();
        ByteArrayInputStream input = new ByteArrayInputStream(commands.getBytes(StandardCharsets.UTF_8));
        ByteArrayOutputStream outputBytes = new ByteArrayOutputStream();
        PrintStream output = new PrintStream(outputBytes, true, "UTF-8");

        ATM atm = new ATM(bank, input, output);
        atm.setEnableInterestCalculation(false);
        atm.start();

        String console = outputBytes.toString("UTF-8");
        Assert.assertTrue(console.contains("取款成功"));
        double balance = bank.queryBalance(bank.getUser("withdrawer"));
        Assert.assertTrue(balance >= 690.0 && balance <= 710.0);
    }

    @Test
    public void passwordChangeFlow() throws Exception {
        Path storage = Files.createTempFile("atm-pwdchange", ".dat");
        Bank bank = new Bank(storage);
        bank.registerUser("pwduser", "PwdUser", User.Gender.UNKNOWN, "oldpass");

        String commands = String.join(System.lineSeparator(),
                "1",
                "pwduser",
                "oldpass",
                "4",
                "oldpass",
                "newpass",
                "0") + System.lineSeparator();
        ByteArrayInputStream input = new ByteArrayInputStream(commands.getBytes(StandardCharsets.UTF_8));
        ByteArrayOutputStream outputBytes = new ByteArrayOutputStream();
        PrintStream output = new PrintStream(outputBytes, true, "UTF-8");

        ATM atm = new ATM(bank, input, output);
        atm.setEnableInterestCalculation(false);
        atm.start();

        String console = outputBytes.toString("UTF-8");
        Assert.assertTrue(console.contains("密码修改成功"));
        Assert.assertTrue(bank.login("pwduser", "newpass") != null);
    }

    @Test
    public void invalidLoginAttempt() throws Exception {
        Path storage = Files.createTempFile("atm-badlogin", ".dat");
        Bank bank = new Bank(storage);
        bank.registerUser("validuser", "有效用户", User.Gender.MALE, "correctpass");

        String commands = String.join(System.lineSeparator(),
                "1",
                "validuser",
                "wrongpass",
                "3") + System.lineSeparator();
        ByteArrayInputStream input = new ByteArrayInputStream(commands.getBytes(StandardCharsets.UTF_8));
        ByteArrayOutputStream outputBytes = new ByteArrayOutputStream();
        PrintStream output = new PrintStream(outputBytes, true, "UTF-8");

        ATM atm = new ATM(bank, input, output);
        atm.setEnableInterestCalculation(false);
        atm.start();

        String console = outputBytes.toString("UTF-8");
        Assert.assertTrue(console.contains("用户名或密码错误"));
    }

    @Test
    public void insufficientBalanceWithdrawal() throws Exception {
        Path storage = Files.createTempFile("atm-insufficient", ".dat");
        Bank bank = new Bank(storage);
        User user = bank.registerUser("pooruser", "PoorUser", User.Gender.FEMALE, "pass");
        bank.deposit(user, 100);

        String commands = String.join(System.lineSeparator(),
                "1",
                "pooruser",
                "pass",
                "2",
                "500",
                "0") + System.lineSeparator();
        ByteArrayInputStream input = new ByteArrayInputStream(commands.getBytes(StandardCharsets.UTF_8));
        ByteArrayOutputStream outputBytes = new ByteArrayOutputStream();
        PrintStream output = new PrintStream(outputBytes, true, "UTF-8");

        ATM atm = new ATM(bank, input, output);
        atm.setEnableInterestCalculation(false);
        atm.start();

        String console = outputBytes.toString("UTF-8");
        Assert.assertTrue(console.contains("取款失败"));
        double balance = bank.queryBalance(bank.getUser("pooruser"));
        Assert.assertTrue(balance >= 95.0 && balance <= 105.0);
    }
}
