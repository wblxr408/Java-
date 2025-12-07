import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertTrue;

class ATMTest {

    @Test
    void runsCompleteMenuFlow() throws IOException {
        Path storage = Files.createTempFile("atm-test", ".dat");
        Files.deleteIfExists(storage);
        Bank bank = new Bank(storage);

        String script = String.join(System.lineSeparator(),
                "1",       // create user
                "tester",
                "测试用户",
                "男",
                "pass123",
                "2",       // login
                "tester",
                "pass123",
                "1",       // deposit
                "300",
                "3",       // query
                "5",       // logout
                "0"        // exit
        ) + System.lineSeparator();

        ByteArrayInputStream input = new ByteArrayInputStream(script.getBytes(StandardCharsets.UTF_8));
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        PrintStream printer = new PrintStream(output, true, StandardCharsets.UTF_8);

        ATM atm = new ATM(bank, input, printer);
        atm.run();

        String console = output.toString(StandardCharsets.UTF_8);
        assertTrue(console.contains("登录成功"));
        assertTrue(console.contains("存入 300 元成功"));
        assertTrue(console.contains("当前余额"));
        assertTrue(console.contains("系统正在退出"));
    }
}
