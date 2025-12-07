import java.io.Console;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.Optional;
import java.util.Scanner;

/**
 * 处理用户与控制台ATM菜单交互的视图层。
 */
public class ATM {
    private final Bank bank;
    private final Scanner scanner;
    private final PrintStream out;
    private volatile User currentUser;
    private volatile boolean running = true;
    private Thread interestThread;
    private final Object displayLock = new Object();

    /**
     * 使用系统输入输出构造 ATM。
     * @param bank 业务控制层
     */
    public ATM(Bank bank) {
        this(bank, System.in, System.out);
    }

    /**
     * 指定输入输出流的构造函数，便于测试。
     *
     * @param bank   业务控制层
     * @param input  输入流
     * @param output 输出流
     */
    public ATM(Bank bank, InputStream input, PrintStream output) {
        this.bank = bank;
        this.out = output;
        this.scanner = new Scanner(new InputStreamReader(input, StandardCharsets.UTF_8));
    }

    /**
     * run！run！run！
     */
    public void run() {
        startInterestThread();
        while (running) {
            if (currentUser == null) {
                showWelcomeMenu();
            } else {
                showUserMenu();
            }
        }
        stopInterestThread();
        persistUsers();
    }

    /**
     * 欢迎菜单。
     */
    private void showWelcomeMenu() {
        clearRealtimeLine();
        printInterestTip();
        out.println("===== XXX银行ATM =====");
        out.println("1. 创建新用户");
        out.println("2. 登录");
        out.println("0. 退出系统");
        String choice = prompt("请选择: ");
        if (choice == null) {
            return;
        }
        switch (choice) {
            case "1" -> createUserFlow();
            case "2" -> loginFlow();
            case "0" -> exitSystem();
            default -> out.println("无效选择，请重新输入。");
        }
    }

    /**
     * 已登录用户菜单。
     */
    private void showUserMenu() {
        clearRealtimeLine();
        out.printf("您好，%s%s！欢迎来到XXX银行的ATM！%n", currentUser.getName(), getGenderSuffix(currentUser));
        out.println("1. 存钱");
        out.println("2. 取钱");
        out.println("3. 查询余额");
        out.println("4. 修改密码");
        out.println("5. 退出当前用户");
        out.println("0. 退出系统");
        String choice = prompt("请输入您的操作: ");
        if (choice == null) {
            return;
        }
        switch (choice) {
            case "1" -> handleDeposit();
            case "2" -> handleWithdrawal();
            case "3" -> showBalance();
            case "4" -> changePassword();
            case "5" -> logout();
            case "0" -> exitSystem();
            default -> out.println("无效选择，请重新输入。");
        }
    }

    /**
     * 处理用户注册流程。
     */
    private void createUserFlow() {
        String username = prompt("请输入用户名: ");
        if (username == null) {
            return;
        }
        String name = prompt("请输入姓名: ");
        if (name == null) {
            return;
        }
        String gender = prompt("请输入性别(男/女): ");
        if (gender == null) {
            return;
        }
        String password = promptPassword("请输入密码: ");
        if (password == null) {
            return;
        }
        try {
            bank.createUser(username.trim(), name.trim(), gender.trim(), password.trim());
            out.println("用户创建成功，请登录。");
        } catch (IllegalArgumentException ex) {
            out.println("创建用户失败: " + ex.getMessage());
        }
    }

    /**
     * 登录流程
     */
    private void loginFlow() {
        String username = prompt("请输入用户名: ");
        if (username == null) {
            return;
        }
        String password = promptPassword("请输入密码: ");
        if (password == null) {
            return;
        }
        Optional<User> user = bank.authenticate(username.trim(), password.trim());
        if (user.isPresent()) {
            currentUser = user.get();
            out.println("登录成功。");
            refreshBalanceDisplay();
        } else {
            out.println("用户名或密码错误。");
        }
    }

    /**
     * 存钱
     */
    private void handleDeposit() {
        int amount = requestAmount("请输入存钱金额: ");
        if (amount <= 0) {
            return;
        }
        try {
            bank.deposit(currentUser, amount);
            out.printf("存入 %d 元成功。%n", amount);
            refreshBalanceDisplay();
        } catch (IllegalArgumentException ex) {
            out.println(ex.getMessage());
        }
    }

    /**
     * 取钱
     */
    private void handleWithdrawal() {
        int amount = requestAmount("请输入取钱金额: ");
        if (amount <= 0) {
            return;
        }
        try {
            bank.withdraw(currentUser, amount);
            out.printf("取出 %d 元成功。%n", amount);
            refreshBalanceDisplay();
        } catch (IllegalArgumentException ex) {
            out.println(ex.getMessage());
        }
    }

    /**
     * 当前余额
     */
    private void showBalance() {
        int balance = bank.queryBalance(currentUser);
        out.printf("当前余额: %d 元%n", balance);
    }

    /**
     * 密码修改
     */
    private void changePassword() {
        String oldPassword = promptPassword("请输入旧密码: ");
        if (oldPassword == null) {
            return;
        }
        String newPassword = promptPassword("请输入新密码: ");
        if (newPassword == null) {
            return;
        }
        try {
            bank.changePassword(currentUser, oldPassword.trim(), newPassword.trim());
            out.println("密码修改成功。");
        } catch (IllegalArgumentException ex) {
            out.println("修改失败: " + ex.getMessage());
        }
    }

    /**
     * 退出当前登录用户。
     */
    private void logout() {
        currentUser = null;
        out.println("已退出当前用户。");
        clearRealtimeLine();
    }

    /**
     * 退出系统并停止后台线程。
     */
    private void exitSystem() {
        clearRealtimeLine();
        running = false;
        out.println("系统正在退出，感谢使用。");
        stopInterestThread();
    }

    /**
     * 请求金额输入并校验。
     * @param prompt 提示语
     * @return 合法金额或 -1
     */
    private int requestAmount(String prompt) {
        String input = prompt(prompt);
        if (input == null) {
            return -1;
        }
        try {
            int amount = Integer.parseInt(input.trim());
            if (amount <= 0) {
                out.println("金额必须大于 0。");
                return -1;
            }
            return amount;
        } catch (NumberFormatException e) {
            out.println("请输入正确的整数金额。");
            return -1;
        }
    }

    /**
     * 输出提示并获取一行输入。
     *
     * @param message 提示语
     * @return 用户输入，为空表示强制退出
     */
    private String prompt(String message) {
        out.print(message);
        if (!scanner.hasNextLine()) {
            running = false;
            out.println();
            return null;
        }
        return scanner.nextLine();
    }

    /**
     * 读取密码输入
     * @param message 提示语
     * @return 密码
     */
    private String promptPassword(String message) {
        Console console = System.console();
        if (console != null) {
            char[] chars = console.readPassword("%s", message);
            if (chars == null) {
                running = false;
                return "";
            }
            String password = new String(chars);
            console.writer().flush();
            return password;
        }
        return prompt(message);
    }

    /**
     * 根据性别返回称谓。
     * @param user 当前用户
     * @return 称谓
     */
    private String getGenderSuffix(User user) {
        String gender = user.getGender().toLowerCase(Locale.ROOT);
        return gender.contains("男") ? "先生" : "女士";
    }

    /**
     * 持久化用户数据。
     */
    private void persistUsers() {
        try {
            bank.saveUsers();
        } catch (IOException e) {
            out.println("保存用户数据失败: " + e.getMessage());
        }
    }

    /**
     * 输出利息说明。
     */
    private void printInterestTip() {
        double seconds = bank.getInterestIntervalMs() / 1000.0;
        double rate = bank.getInterestRate() * 100;
        out.printf("提示：后台线程每 %.1f 秒按 %.2f%% 利率结息并实时覆盖余额显示。%n", seconds, rate);
    }

    /**
     * 启动后台利息线程。
     */
    private void startInterestThread() {
        if (interestThread != null) {
            return;
        }
        interestThread = new Thread(() -> {
            while (running) {
                try {
                    Thread.sleep(bank.getInterestIntervalMs());
                    boolean changed = bank.applyInterestToAllUsers();
                    if (changed) {
                        refreshBalanceDisplay();
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }, "ATM-Interest-Updater");
        interestThread.setDaemon(true);
        interestThread.start();
    }

    /**
     * 停止后台利息线程。
     */
    private void stopInterestThread() {
        if (interestThread == null) {
            return;
        }
        interestThread.interrupt();
        try {
            interestThread.join(100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        interestThread = null;
    }

    /**
     * 刷新实时余额显示。
     */
    private void refreshBalanceDisplay() {
        if (currentUser == null) {
            return;
        }
        synchronized (displayLock) {
            int balance = bank.queryBalance(currentUser);
            out.print("\r实时余额（含利息）: " + balance + " 元         ");
            out.flush();
        }
    }

    /**
     * 清空实时显示行，避免覆盖其他输出。
     */
    private void clearRealtimeLine() {
        synchronized (displayLock) {
            out.print("\r");
            out.print(" ".repeat(40));
            out.print("\r");
            out.flush();
        }
    }
}
