import java.nio.file.Path;

/**
 * 应用入口类。
 */
public class SimpleATM {
    private static final Path STORAGE_PATH = Path.of("data", "users.dat");
    /**
     * 程序入口，创建 Bank 与 ATM 并运行。
     * @param args 命令行参数
     */
    public static void main(String[] args) {
        Bank bank = new Bank(STORAGE_PATH);
        ATM atm = new ATM(bank);
        atm.run();
    }
}
