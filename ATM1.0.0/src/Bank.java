import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * 业务控制层
 */
public class Bank {
    private final Map<String, User> users = new HashMap<>();
    private final Path storageFile;
    private final double interestRate;
    private final long interestIntervalMs;

    /**
     * 使用默认利率/周期创建银行控制器。
     *
     * @param storageFile 存档路径
     */
    public Bank(Path storageFile) {
        this(storageFile, 0.02d, 5_000L);
    }

    /**
     * 自定义利率和周期的构造函数。
     *
     * @param storageFile 存档路径
     * @param interestRate 利率
     * @param interestIntervalMs 计息周期
     */
    public Bank(Path storageFile, double interestRate, long interestIntervalMs) {
        this.storageFile = storageFile;
        this.interestRate = interestRate;
        this.interestIntervalMs = interestIntervalMs;
        loadUsers();
        ensureDefaultAdmin();
    }

    /**
     * 返回不可变的用户视图。
     *
     * @return 用户映射
     */
    public synchronized Map<String, User> getUsersView() {
        return Collections.unmodifiableMap(users);
    }

    /**
     * 创建新用户。
     *
     * @param username 用户名
     * @param name 姓名
     * @param gender 性别
     * @param password 密码
     * @return 新用户
     */
    public synchronized User createUser(String username, String name, String gender, String password) {
        String normalizedUsername = normalize(username);
        Objects.requireNonNull(name, "name");
        Objects.requireNonNull(gender, "gender");
        Objects.requireNonNull(password, "password");
        if (users.containsKey(normalizedUsername)) {
            throw new IllegalArgumentException("用户名已存在");
        }
        User user = new User(normalizedUsername, name.trim(), gender.trim(), password, LocalDateTime.now());
        users.put(normalizedUsername, user);
        return user;
    }

    /**
     * 进行登录校验。
     *
     * @param username 用户名
     * @param password 密码
     * @return 匹配的用户
     */
    public synchronized Optional<User> authenticate(String username, String password) {
        if (username == null || password == null) {
            return Optional.empty();
        }
        User user = users.get(username.trim());
        if (user != null && user.passwordMatches(password)) {
            return Optional.of(user);
        }
        return Optional.empty();
    }

    /**
     * 查询余额。
     *
     * @param user 用户
     * @return 余额
     */
    public synchronized int queryBalance(User user) {
        return requireUser(user).getBalance();
    }

    /**
     * 为全部用户结算利息。
     * @return 是否有余额变化
     */
    public synchronized boolean applyInterestToAllUsers() {
        boolean changed = false;
        for (User user : users.values()) {
            int delta = user.applyInterest(interestRate);
            if (delta > 0) {
                changed = true;
            }
        }
        return changed;
    }

    /**
     * 存钱。
     *
     * @param user 用户
     * @param amount 金额
     */
    public synchronized void deposit(User user, int amount) {
        User managedUser = requireUser(user);
        if (amount <= 0) {
            throw new IllegalArgumentException("存钱金额必须大于 0");
        }
        managedUser.credit(amount);
    }

    /**
     * 取钱。
     *
     * @param user 用户
     * @param amount 金额
     */
    public synchronized void withdraw(User user, int amount) {
        User managedUser = requireUser(user);
        if (amount <= 0) {
            throw new IllegalArgumentException("取钱金额必须大于 0");
        }
        if (amount > managedUser.getBalance()) {
            throw new IllegalArgumentException("余额不足");
        }
        managedUser.debit(amount);
    }

    /**
     * 修改密码。
     * @param user 用户
     * @param oldPassword 旧密码
     * @param newPassword 新密码
     */
    public synchronized void changePassword(User user, String oldPassword, String newPassword) {
        User managedUser = requireUser(user);
        if (!managedUser.passwordMatches(oldPassword)) {
            throw new IllegalArgumentException("旧密码错误");
        }
        if (newPassword == null || newPassword.isBlank()) {
            throw new IllegalArgumentException("新密码不能为空");
        }
        managedUser.updatePassword(newPassword);
    }

    /**
     * 持久化用户数据。
     * @throws IOException IO 异常
     */
    public synchronized void saveUsers() throws IOException {
        Path parent = storageFile.getParent();
        if (parent != null) {
            Files.createDirectories(parent);
        }
        try (ObjectOutputStream outputStream = new ObjectOutputStream(new FileOutputStream(storageFile.toFile()))) {
            outputStream.writeObject(users);
        }
    }

    @SuppressWarnings("unchecked")
    /**
     * 从磁盘加载用户信息。
     */
    private void loadUsers() {
        if (Files.notExists(storageFile)) {
            return;
        }
        try (ObjectInputStream inputStream = new ObjectInputStream(new FileInputStream(storageFile.toFile()))) {
            Object data = inputStream.readObject();
            if (data instanceof Map<?, ?> map) {
                map.forEach((key, value) -> {
                    if (key instanceof String userKey && value instanceof User userValue) {
                        users.put(userKey, userValue);
                    }
                });
            }
        } catch (IOException | ClassNotFoundException e) {
            users.clear();
        }
    }

    /**
     * 确保默认管理员存在。
     */
    private void ensureDefaultAdmin() {
        if (!users.containsKey("admin")) {
            User admin = new User("admin", "管理员", "男", "123456", LocalDateTime.now());
            users.put("admin", admin);
        }
    }

    /**
     * 检查用户是否受管。
     * @param user 用户
     * @return 管理对象
     */
    private User requireUser(User user) {
        if (user == null || !users.containsKey(user.getUsername())) {
            throw new IllegalArgumentException("无效用户");
        }
        return users.get(user.getUsername());
    }

    /**
     * 去除空白并验证字符串。
     * @param value 待验证值
     * @return 规范化字符串
     */
    private String normalize(String value) {
        if (value == null) {
            throw new IllegalArgumentException("用户名不能为空");
        }
        String trimmed = value.trim();
        if (trimmed.isEmpty()) {
            throw new IllegalArgumentException("用户名不能为空");
        }
        return trimmed;
    }

    /**
     * 获取计息周期。
     * @return 周期毫秒
     */
    public long getInterestIntervalMs() {
        return interestIntervalMs;
    }

    /**
     * 获取利率。
     * @return 利率
     */
    public double getInterestRate() {
        return interestRate;
    }
}
