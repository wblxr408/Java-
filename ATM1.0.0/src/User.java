import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * 用户层
 */
public class User implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private final String username;
    private final String name;
    private final String gender;
    private String password;
    private final LocalDateTime createdAt;
    private int balance;

    /**
     * 构造一个用户实体。
     * @param username 用户名
     * @param name     姓名
     * @param gender   性别
     * @param password 密码
     * @param createdAt 创建时间
     */
    public User(String username, String name, String gender, String password, LocalDateTime createdAt) {
        this.username = Objects.requireNonNull(username, "username");
        this.name = Objects.requireNonNull(name, "name");
        this.gender = Objects.requireNonNull(gender, "gender");
        this.password = Objects.requireNonNull(password, "password");
        this.createdAt = Objects.requireNonNullElseGet(createdAt, LocalDateTime::now);
        this.balance = 0;
    }

    /**
     * 获取用户名。
     * @return 用户名
     */
    public String getUsername() {
        return username;
    }

    /**
     * 获取姓名。
     * @return 姓名
     */
    public String getName() {
        return name;
    }

    /**
     * 获取性别描述。
     * @return 性别
     */
    public String getGender() {
        return gender;
    }

    /**
     * 获取账户创建时间。
     * @return 创建时间
     */
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    /**
     * 获取账户余额。
     * @return 当前余额
     */
    public synchronized int getBalance() {
        return balance;
    }

    /**
     * 记入金额。
     * @param amount 金额
     */
    synchronized void credit(int amount) {
        balance += amount;
    }

    /**
     * 扣减金额。
     * @param amount 金额
     */
    synchronized void debit(int amount) {
        balance -= amount;
    }

    /**
     * 根据利率计算并记入利息。
     * @param rate 利率
     * @return 实际增加的金额
     */
    synchronized int applyInterest(double rate) {
        if (balance <= 0 || rate <= 0) {
            return 0;
        }
        int interest = (int) Math.round(balance * rate);
        if (interest > 0) {
            balance += interest;
        }
        return interest;
    }

    /**
     * 判断密码是否一致。
     * @param rawPassword 待比对的明文
     * @return 是否匹配
     */
    public synchronized boolean passwordMatches(String rawPassword) {
        return Objects.equals(password, rawPassword);
    }

    /**
     * 更新用户密码。
     * @param newPassword 新密码
     */
    public synchronized void updatePassword(String newPassword) {
        password = Objects.requireNonNull(newPassword, "newPassword");
    }
}
