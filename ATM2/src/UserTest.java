import java.time.LocalDateTime;

import org.junit.Assert;
import org.junit.Test;

public class UserTest {
    @Test
    public void shouldStoreCoreInformation() {
        LocalDateTime start = LocalDateTime.now().minusSeconds(1);
        User user = new User("tester", "张三", User.Gender.MALE, "123456");
        Assert.assertEquals("tester", user.getUsername());
        Assert.assertEquals("张三", user.getName());
        Assert.assertEquals(User.Gender.MALE, user.getGender());
        Assert.assertEquals("123456", user.getPassword());
        Assert.assertTrue(user.getCreatedAt().isAfter(start) || user.getCreatedAt().isEqual(start));
    }

    @Test
    public void genderParsingUnderstandsCommonValues() {
        Assert.assertEquals(User.Gender.MALE, User.Gender.fromInput("男"));
        Assert.assertEquals(User.Gender.FEMALE, User.Gender.fromInput("F"));
        Assert.assertEquals(User.Gender.UNKNOWN, User.Gender.fromInput("其他"));
    }

    @Test
    public void shouldAllowPasswordChange() {
        User user = new User("user1", "李四", User.Gender.FEMALE, "oldpass");
        Assert.assertEquals("oldpass", user.getPassword());
        user.setPassword("newpass");
        Assert.assertEquals("newpass", user.getPassword());
    }

    @Test
    public void shouldAllowNameChange() {
        User user = new User("user2", "王五", User.Gender.MALE, "pass");
        Assert.assertEquals("王五", user.getName());
        user.setName("王小五");
        Assert.assertEquals("王小五", user.getName());
    }

    @Test
    public void shouldInitializeWithZeroBalance() {
        User user = new User("user3", "赵六", User.Gender.UNKNOWN, "123");
        Assert.assertEquals(0.0, user.getBalance(), 0.0001);
    }

    @Test
    public void shouldHandleBalanceOperations() {
        User user = new User("user4", "孙七", User.Gender.MALE, "pass");
        user.increaseBalance(100.0);
        Assert.assertEquals(100.0, user.getBalance(), 0.0001);
        user.decreaseBalance(30.0);
        Assert.assertEquals(70.0, user.getBalance(), 0.0001);
    }

    @Test
    public void genderHonorificsShouldBeCorrect() {
        Assert.assertEquals("先生", User.Gender.MALE.getHonorific());
        Assert.assertEquals("女士", User.Gender.FEMALE.getHonorific());
        Assert.assertEquals("用户", User.Gender.UNKNOWN.getHonorific());
    }
}
