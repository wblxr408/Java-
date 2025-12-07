/**
 * ATM系统演示程序
 * 展示MVC架构的三层结构和主要功能
 */
public class ATMDemo {
    public static void main(String[] args) {
        System.out.println("========================================");
        System.out.println("    ATM系统演示 - MVC架构实现");
        System.out.println("========================================\n");

        // 创建Bank控制层
        Bank bank = new Bank();
        bank.loadUsers();
        
        // 设置利率为0.01% (每次计算增加0.01%)
        bank.setInterestRate(0.0001);
        
        System.out.println("【模型层 - User类】");
        System.out.println("存储用户信息：用户名、姓名、性别、密码、账号创建时间、余额");
        
        // 创建测试用户
        User user1 = bank.registerUser("zhangsan", "张三", User.Gender.MALE, "pass123");
        User user2 = bank.registerUser("lisi", "李四", User.Gender.FEMALE, "pass456");
        
        if (user1 != null) {
            System.out.println("✓ 创建用户: " + user1.getUsername() + " - " + user1.getName() + 
                             user1.getGender().getHonorific());
        }
        if (user2 != null) {
            System.out.println("✓ 创建用户: " + user2.getUsername() + " - " + user2.getName() + 
                             user2.getGender().getHonorific());
        }
        
        System.out.println("\n【控制层 - Bank类】");
        System.out.println("完成用户要求的查询余额、存钱、取钱等操作");
        
        // 存钱操作
        System.out.println("\n1. 存钱操作:");
        if (bank.deposit(user1, 1000)) {
            System.out.println("  ✓ " + user1.getName() + " 存入 1000 元");
            System.out.println("  当前余额: " + bank.queryBalance(user1) + " 元");
        }
        
        if (bank.deposit(user2, 2000)) {
            System.out.println("  ✓ " + user2.getName() + " 存入 2000 元");
            System.out.println("  当前余额: " + bank.queryBalance(user2) + " 元");
        }
        
        // 取钱操作
        System.out.println("\n2. 取钱操作:");
        if (bank.withdrawl(user1, 300)) {
            System.out.println("  ✓ " + user1.getName() + " 取出 300 元");
            System.out.println("  当前余额: " + bank.queryBalance(user1) + " 元");
        }
        
        // 非法取钱
        System.out.println("\n3. 非法操作验证:");
        if (!bank.withdrawl(user1, 5000)) {
            System.out.println("  ✓ 余额不足，无法取出 5000 元");
        }
        if (!bank.deposit(user1, -100)) {
            System.out.println("  ✓ 金额非法，无法存入 -100 元");
        }
        
        // 查询余额
        System.out.println("\n4. 查询余额:");
        System.out.println("  " + user1.getName() + " 的余额: " + bank.queryBalance(user1) + " 元");
        System.out.println("  " + user2.getName() + " 的余额: " + bank.queryBalance(user2) + " 元");
        
        // 利率计算演示
        System.out.println("\n【利率计算功能】");
        System.out.println("当前利率: " + String.format("%.4f%%", bank.getInterestRate() * 100));
        System.out.println("启动自动利率计算线程...");
        bank.startInterestCalculation(2000); // 每2秒计算一次
        
        try {
            System.out.println("等待6秒，观察余额变化...");
            for (int i = 1; i <= 3; i++) {
                Thread.sleep(2000);
                System.out.println("  第" + i + "次计算后:");
                System.out.println("    " + user1.getName() + " 余额: " + 
                                 String.format("%.2f", bank.queryBalance(user1)) + " 元");
                System.out.println("    " + user2.getName() + " 余额: " + 
                                 String.format("%.2f", bank.queryBalance(user2)) + " 元");
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        
        bank.stopInterestCalculation();
        System.out.println("利率计算线程已停止");
        
        // 持久化演示
        System.out.println("\n【持久化功能】");
        bank.saveUsers();
        System.out.println("✓ 用户数据已保存到文件");
        
        // 重新加载
        Bank newBank = new Bank();
        newBank.loadUsers();
        User reloadedUser = newBank.getUser("zhangsan");
        if (reloadedUser != null) {
            System.out.println("✓ 从文件加载用户数据成功");
            System.out.println("  " + reloadedUser.getName() + " 的余额: " + 
                             String.format("%.2f", newBank.queryBalance(reloadedUser)) + " 元");
        }
        
        System.out.println("\n【视图层 - ATM类】");
        System.out.println("向用户呈现菜单，接收用户输入，显示信息");
        System.out.println("运行 ATM.main() 可以启动完整的交互式界面");
        
        System.out.println("\n========================================");
        System.out.println("    演示完成！");
        System.out.println("========================================");
    }
}
