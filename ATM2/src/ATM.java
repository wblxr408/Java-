import java.io.InputStream;
import java.io.PrintStream;
import java.util.Scanner;

/**
 * ATM view implementation that interacts with the user through a console menu.
 */
public class ATM {
    private final Bank bank;
    private final Scanner scanner;
    private final PrintStream out;
    private User currentUser;
    private boolean enableInterestCalculation = true;

    public ATM() {
        this(new Bank(), System.in, System.out);
    }

    public ATM(Bank bank, InputStream input, PrintStream output) {
        this.bank = bank;
        this.scanner = new Scanner(input);
        this.out = output;
    }

    public void setEnableInterestCalculation(boolean enable) {
        this.enableInterestCalculation = enable;
    }

    public void start() {
        bank.loadUsers();
        // 启动利率计算线程，每10秒计算一次利息
        if (enableInterestCalculation) {
            bank.startInterestCalculation(10000);
        }
        boolean running = true;
        while (running) {
            running = currentUser == null ? handleGuestMenu() : handleUserMenu();
        }
        if (enableInterestCalculation) {
            bank.stopInterestCalculation();
        }
        bank.saveUsers();
        out.println("用户信息已保存，欢迎下次再来！");
    }

    private boolean handleGuestMenu() {
        out.println("\n===== 欢迎来到XXX银行的ATM =====");
        out.println("1. 登录");
        out.println("2. 创建新用户");
        out.println("3. 退出系统");
        int choice = readInt("请输入您的选择: ");
        switch (choice) {
            case 1:
                handleLogin();
                return true;
            case 2:
                handleRegistration();
                return true;
            case 3:
                out.println("感谢使用，正在退出...");
                return false;
            default:
                out.println("无效的菜单选项，请重新输入。");
                return true;
        }
    }

    private boolean handleUserMenu() {
        out.println("\n您好，" + currentUser.getName() + currentUser.getGender().getHonorific() + "！欢迎来到XXX银行的ATM！");
        out.println("当前余额: " + formatBalance(bank.queryBalance(currentUser)) + " (利率: " + formatInterestRate(bank.getInterestRate()) + ")");
        out.println("1. 存钱");
        out.println("2. 取钱");
        out.println("3. 查询余额");
        out.println("4. 修改密码");
        out.println("5. 退出当前用户");
        out.println("0. 退出系统");
        int choice = readInt("请输入您的操作: ");
        switch (choice) {
            case 1:
                handleDeposit();
                return true;
            case 2:
                handleWithdraw();
                return true;
            case 3:
                showBalance();
                return true;
            case 4:
                handlePasswordChange();
                return true;
            case 5:
                logoutCurrentUser();
                return true;
            case 0:
                out.println("正在退出系统...");
                logoutCurrentUser();
                return false;
            default:
                out.println("无效的菜单选项，请重新输入。");
                return true;
        }
    }

    private void handleLogin() {
        String username = readLine("请输入用户名: ");
        String password = readLine("请输入密码: ");
        User user = bank.login(username, password);
        if (user == null) {
            out.println("用户名或密码错误。");
        } else {
            currentUser = user;
            out.println("登录成功！");
            out.println("您好，" + currentUser.getName() + currentUser.getGender().getHonorific() + "！欢迎来到XXX银行的ATM！");
            showBalance();
        }
    }

    private void handleRegistration() {
        String username = readLine("请选择一个用户名: ");
        if (bank.userExists(username)) {
            out.println("该用户名已存在，请重新选择。");
            return;
        }
        String name = readLine("请输入姓名: ");
        String genderInput = readLine("请输入性别(男/女): ");
        String password = readLine("请输入密码: ");
        User.Gender gender = User.Gender.fromInput(genderInput);
        User newUser = bank.registerUser(username, name, gender, password);
        if (newUser == null) {
            out.println("创建用户失败，请检查输入。");
            return;
        }
        out.println("用户创建成功，请重新登录。");
    }

    private void handleDeposit() {
        int amount = readPositiveAmount("请输入存款金额: ");
        if (bank.deposit(currentUser, amount)) {
            out.println("存款成功，当前余额: " + formatBalance(bank.queryBalance(currentUser)));
        } else {
            out.println("金额不合法，存款失败。");
        }
    }

    private void handleWithdraw() {
        int amount = readPositiveAmount("请输入取款金额: ");
        if (bank.withdrawl(currentUser, amount)) {
            out.println("取款成功，当前余额: " + formatBalance(bank.queryBalance(currentUser)));
        } else {
            out.println("取款失败：金额非法或余额不足。");
        }
    }

    private void showBalance() {
        out.println("当前余额: " + formatBalance(bank.queryBalance(currentUser)));
    }

    private void handlePasswordChange() {
        String oldPassword = readLine("请输入原密码: ");
        String newPassword = readLine("请输入新密码: ");
        if (bank.changePassword(currentUser, oldPassword, newPassword)) {
            out.println("密码修改成功。");
        } else {
            out.println("密码修改失败，请确认原密码正确且新密码不为空。");
        }
    }

    private void logoutCurrentUser() {
        if (currentUser != null) {
            out.println("用户 " + currentUser.getUsername() + " 已退出。");
        }
        currentUser = null;
    }

    private int readInt(String prompt) {
        while (true) {
            out.print(prompt);
            String line = readRawLine();
            if (line == null) {
                return 0;
            }
            try {
                return Integer.parseInt(line.trim());
            } catch (NumberFormatException e) {
                out.println("请输入有效的数字。");
            }
        }
    }

    private int readPositiveAmount(String prompt) {
        while (true) {
            int value = readInt(prompt);
            if (value > 0) {
                return value;
            }
            if (value == 0 && !scanner.hasNextLine()) {
                return 0;
            }
            out.println("金额必须是正整数。");
        }
    }

    private String readLine(String prompt) {
        out.print(prompt);
        String line = readRawLine();
        return line == null ? "" : line.trim();
    }

    private String readRawLine() {
        if (!scanner.hasNextLine()) {
            return null;
        }
        return scanner.nextLine();
    }

    protected Bank getBank() {
        return bank;
    }

    protected PrintStream getOutput() {
        return out;
    }

    protected synchronized User getCurrentUserSnapshot() {
        return currentUser;
    }

    private String formatBalance(double value) {
        return String.format("%.2f", value);
    }

    private String formatInterestRate(double rate) {
        return String.format("%.4f%%", rate * 100);
    }

    public static void main(String[] args) {
        new ATM().start();
    }
}
