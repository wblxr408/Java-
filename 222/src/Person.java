public class Person {
    private String name;
    private int age;

    private static final int MIN_AGE = 0;
    private static final int MAX_AGE = 150;
    private static final int MIN_NAME_LENGTH = 1;
    private static final int MAX_NAME_LENGTH = 50;
    private static final String NAME_PATTERN = "^[\\u4e00-\\u9fa5a-zA-Z]+(\\s+[\\u4e00-\\u9fa5a-zA-Z]+)*$";

    /**
     * 构造函数
     * @param name 姓名
     * @param age 年龄
     */
    public Person(String name, int age) {
        this.name = name;
        this.age = age;
    }

    /**
     * 打印人员信息
     */
    public void greet() {
        System.out.println("Hello, I am " + name + ", and I am " + age + " years old");
    }

    /**
     * 静态方法：通过命令行参数创建Person对象
     * @param args 命令行参数数组
     * @return 创建的Person对象，如果参数无效返回null
     */
    public static Person createFromArgs(String[] args) {
        if (args.length < 2) {
            System.out.println("错误: 请提供姓名和年龄参数");
            return null;
        }

        String name = args[0].trim();
        String ageStr = args[1].trim();

        if (!isValidName(name) || !isValidAge(ageStr)) {
            return null;
        }

        int age = Integer.parseInt(ageStr);
        return new Person(name, age);
    }

    public static void main(String[] args) {
        Person person = Person.createFromArgs(args);
        if (person != null) {
            person.greet();
        }
    }

    /**
     * 验证姓名是否有效
     * @param name 姓名
     * @return 如果有效返回true，否则返回false
     */
    private static boolean isValidName(String name) {
        if (name.isEmpty()) {
            System.out.println("错误: 姓名不能为空");
            return false;
        }

        if (name.length() < MIN_NAME_LENGTH || name.length() > MAX_NAME_LENGTH) {
            System.out.println("错误: 姓名长度必须在" + MIN_NAME_LENGTH + "-" + MAX_NAME_LENGTH + "个字符之间");
            return false;
        }

        if (!name.matches(NAME_PATTERN)) {
            System.out.println("错误: 姓名只能包含中文、英文字母，单词间用单个空格分隔");
            return false;
        }

        if (name.contains("  ")) {
            System.out.println("错误: 姓名中不能包含连续的空格");
            return false;
        }

        if (!name.equals(name.trim())) {
            System.out.println("错误: 姓名首尾不能包含空格");
            return false;
        }

        if (name.replaceAll("\\s+", "").isEmpty()) {
            System.out.println("错误: 姓名不能只包含空格");
            return false;
        }

        return true;
    }

    /**
     * 验证年龄字符串是否有效
     * @param ageStr 年龄字符串
     * @return 如果有效返回true，否则返回false
     */
    private static boolean isValidAge(String ageStr) {
        if (ageStr.isEmpty()) {
            System.out.println("错误: 年龄不能为空");
            return false;
        }

        if (!ageStr.matches("^\\d+$")) {
            System.out.println("错误: 年龄只能包含数字，不能包含小数点、负号或其他字符");
            return false;
        }

        if (ageStr.length() > 1 && ageStr.startsWith("0")) {
            System.out.println("错误: 年龄不能有前导零");
            return false;
        }

        try {
            int age = Integer.parseInt(ageStr);

            if (age < MIN_AGE) {
                System.out.println("错误: 年龄不能小于" + MIN_AGE + "岁");
                return false;
            }

            if (age > MAX_AGE) {
                System.out.println("错误: 年龄不能大于" + MAX_AGE + "岁");
                return false;
            }

            return true;

        } catch (NumberFormatException e) {
            System.out.println("错误: 年龄数值过大，请输入合理的数字");
            return false;
        }
    }
}