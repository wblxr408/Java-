/**
 * 银行账户类。
 * 提供存款、取款和自动利息计算功能。
 */
public class BankAccount {
    private double balance;
    private final double interestRate;

    /**
     * 构造一个带初始余额与利率的账户对象。
     * @param initialBalance 初始余额
     * @param interestRate   年利率
     */
    public BankAccount(double initialBalance, double interestRate) {
        this.balance = initialBalance;
        this.interestRate = interestRate;
    }

    /** 向账户中存入指定金额。 */
    public synchronized void deposit(double amount) {
        if (amount > 0) {
            balance += amount;
            System.out.println(" 存款成功！");
        } else {
            System.out.println("️ 存款金额必须大于 0！");
        }
    }

    /** 如果余额充足，从账户中取出指定金额。 */
    public synchronized void withdraw(double amount) {
        if (amount > 0 && amount <= balance) {
            balance -= amount;
            System.out.println(" 取款成功！");
        } else if (amount > balance) {
            System.out.println(" 余额不足！");
        } else {
            System.out.println("️ 取款金额必须大于 0！");
        }
    }

    /** 获取当前余额。 */
    public synchronized double getBalance() {
        return balance;
    }

    /** 计算复利并更新余额。 */
    public synchronized void addInterest() {
        balance += balance * interestRate;
    }
}
