import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Bank controller that coordinates all balance related operations and keeps the in-memory store for users.
 */
public class Bank implements Serializable {
    private static final long serialVersionUID = 1L;

    private final Map<String, User> users = new HashMap<>();
    private final Path storageFile;
    private double interestRate = 0.0001; // 默认利率 0.01%
    private transient Thread interestThread;
    private transient volatile boolean interestThreadRunning = false;

    public Bank() {
        this(Paths.get("atm-users.dat"));
    }

    public Bank(Path storageFile) {
        this.storageFile = storageFile;
    }

    public synchronized void setInterestRate(double rate) {
        if (rate >= 0) {
            this.interestRate = rate;
        }
    }

    public synchronized double getInterestRate() {
        return interestRate;
    }

    public synchronized void loadUsers() {
        users.clear();
        if (Files.exists(storageFile)) {
            try (ObjectInputStream input = new ObjectInputStream(Files.newInputStream(storageFile))) {
                Object raw = input.readObject();
                if (raw instanceof Map<?, ?>) {
                    Map<?, ?> map = (Map<?, ?>) raw;
                    map.forEach((key, value) -> {
                        if (key instanceof String && value instanceof User) {
                            users.put((String) key, (User) value);
                        }
                    });
                }
            } catch (IOException | ClassNotFoundException e) {
                System.out.println("⚠ 无法加载已有用户信息，将创建默认管理员账户。");
            }
        }
        ensureDefaultAdmin();
    }

    /**
     * 启动利率计算线程，定期为所有用户计算利息
     * @param intervalMillis 计算间隔（毫秒）
     */
    public synchronized void startInterestCalculation(long intervalMillis) {
        if (interestThreadRunning) {
            return;
        }
        interestThreadRunning = true;
        interestThread = new Thread(() -> {
            while (interestThreadRunning) {
                try {
                    Thread.sleep(intervalMillis);
                    applyInterestToAllUsers(interestRate);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }, "InterestCalculationThread");
        interestThread.setDaemon(true);
        interestThread.start();
    }

    /**
     * 停止利率计算线程
     */
    public synchronized void stopInterestCalculation() {
        interestThreadRunning = false;
        if (interestThread != null) {
            interestThread.interrupt();
            try {
                interestThread.join(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            interestThread = null;
        }
    }

    private void ensureDefaultAdmin() {
        if (!users.containsKey("admin")) {
            users.put("admin", new User("admin", "管理员", User.Gender.UNKNOWN, "123456", LocalDateTime.now(), 0));
        }
    }

    public synchronized void saveUsers() {
        try {
            if (storageFile.getParent() != null) {
                Files.createDirectories(storageFile.getParent());
            }
            try (ObjectOutputStream output = new ObjectOutputStream(Files.newOutputStream(storageFile))) {
                output.writeObject(new HashMap<>(users));
            }
        } catch (IOException e) {
            System.out.println("⚠ 保存用户信息失败: " + e.getMessage());
        }
    }

    public synchronized boolean userExists(String username) {
        if (username == null) {
            return false;
        }
        return users.containsKey(username.trim());
    }

    public synchronized User registerUser(String username, String name, User.Gender gender, String password) {
        if (username == null || username.isBlank()) {
            return null;
        }
        String normalized = username.trim();
        if (users.containsKey(normalized)) {
            return null;
        }
        User user = new User(normalized, name, gender, password);
        users.put(user.getUsername(), user);
        return user;
    }

    public synchronized User login(String username, String password) {
        if (username == null || password == null) {
            return null;
        }
        User user = users.get(username.trim());
        if (user != null && password.equals(user.getPassword())) {
            return user;
        }
        return null;
    }

    public synchronized double queryBalance(User user) {
        User stored = resolveUser(user);
        return stored == null ? 0 : stored.getBalance();
    }

    public synchronized boolean deposit(User user, int amount) {
        User stored = resolveUser(user);
        if (stored == null || amount <= 0) {
            return false;
        }
        stored.increaseBalance(amount);
        return true;
    }

    public synchronized boolean withdrawl(User user, int amount) {
        User stored = resolveUser(user);
        if (stored == null || amount <= 0 || amount > stored.getBalance()) {
            return false;
        }
        stored.decreaseBalance(amount);
        return true;
    }

    public synchronized void applyInterestToAllUsers(double interestRate) {
        if (interestRate <= 0) {
            return;
        }
        users.values().forEach(user -> {
            double currentBalance = user.getBalance();
            if (currentBalance > 0) {
                double interest = currentBalance * interestRate;
                if (interest > 0) {
                    user.increaseBalance(interest);
                }
            }
        });
    }

    public synchronized boolean changePassword(User user, String oldPassword, String newPassword) {
        if (newPassword == null || newPassword.isBlank()) {
            return false;
        }
        User stored = resolveUser(user);
        if (stored != null && stored.getPassword().equals(oldPassword)) {
            stored.setPassword(newPassword);
            return true;
        }
        return false;
    }

    public synchronized User getUser(String username) {
        if (username == null) {
            return null;
        }
        return users.get(username.trim());
    }

    private User resolveUser(User user) {
        if (user == null) {
            return null;
        }
        return users.get(user.getUsername());
    }
}
