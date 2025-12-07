import java.io.InputStream;
import java.io.PrintStream;

/**
 * ATMService now focuses on retaining the legacy interest/real-time balance updates
 * while delegating user interaction to the modern ATM implementation.
 */
public class ATMService extends ATM {
    private static final double DEFAULT_INTEREST_RATE = 0.02;
    private static final int YEAR_DURATION_MS = 5000;

    private Thread interestThread;

    public ATMService() {
        super();
    }

    public ATMService(Bank bank, InputStream input, PrintStream output) {
        super(bank, input, output);
    }

    @Override
    public void start() {
        startInterestThread();
        try {
            super.start();
        } finally {
            stopInterestThread();
        }
    }

    private void startInterestThread() {
        interestThread = new Thread(() -> {
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    Thread.sleep(YEAR_DURATION_MS);
                    getBank().applyInterestToAllUsers(DEFAULT_INTEREST_RATE);
                    updateBalanceDisplay();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }, "atm-interest-updater");
        interestThread.setDaemon(true);
        interestThread.start();
    }

    private void stopInterestThread() {
        if (interestThread != null) {
            interestThread.interrupt();
        }
    }

    private void updateBalanceDisplay() {
        User current = getCurrentUserSnapshot();
        if (current == null) {
            return;
        }
        double balance = getBank().queryBalance(current);
        PrintStream out = getOutput();
        out.print("\r 当前余额: " + String.format("%.2f", balance) + "    ");
        out.flush();
    }
}
