import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * User model that keeps track of the core information that belongs to a bank customer.
 */
public class User implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * Lightweight representation of gender so the ATM can greet users politely.
     */
    public enum Gender {
        MALE("先生"),
        FEMALE("女士"),
        UNKNOWN("用户");

        private final String honorific;

        Gender(String honorific) {
            this.honorific = honorific;
        }

        public String getHonorific() {
            return honorific;
        }

        /**
         * Converts loosely formatted user input into a gender constant.
         */
        public static Gender fromInput(String raw) {
            if (raw == null) {
                return UNKNOWN;
            }
            String normalized = raw.trim().toLowerCase();
            if (normalized.startsWith("男") || normalized.startsWith("m")) {
                return MALE;
            }
            if (normalized.startsWith("女") || normalized.startsWith("f")) {
                return FEMALE;
            }
            return UNKNOWN;
        }
    }

    private final String username;
    private String name;
    private Gender gender;
    private String password;
    private final LocalDateTime createdAt;
    private double balance;

    public User(String username, String name, Gender gender, String password) {
        this(username, name, gender, password, LocalDateTime.now(), 0);
    }

    public User(String username, String name, Gender gender, String password,
                LocalDateTime createdAt, double balance) {
        this.username = Objects.requireNonNull(username, "username").trim();
        this.name = Objects.requireNonNull(name, "name").trim();
        this.gender = gender == null ? Gender.UNKNOWN : gender;
        this.password = Objects.requireNonNull(password, "password");
        this.createdAt = createdAt == null ? LocalDateTime.now() : createdAt;
        this.balance = Math.max(0, balance);
    }

    public String getUsername() {
        return username;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = Objects.requireNonNull(name, "name").trim();
    }

    public Gender getGender() {
        return gender;
    }

    public void setGender(Gender gender) {
        this.gender = gender == null ? Gender.UNKNOWN : gender;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = Objects.requireNonNull(password, "password");
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public synchronized double getBalance() {
        return balance;
    }

    synchronized void increaseBalance(double amount) {
        balance += amount;
    }

    synchronized void decreaseBalance(double amount) {
        balance -= amount;
    }
}
