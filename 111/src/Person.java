import java.util.Scanner;

/**
 * Person类用于创建人员对象并展示基本信息
 */
public class Person {
    private String name;
    private int age;

    // 静态Scanner对象
    private static Scanner scanner = new Scanner(System.in);

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
     * 静态方法：创建Person对象并获取用户输入
     * @return 创建的Person对象
     */
    public static Person createPersonFromInput() {
        String name = getValidName();
        int age = getValidAge();
        return new Person(name, age);
    }

    public static void main(String[] args) {
        Person person = Person.createPersonFromInput();
        person.greet();
    }

    /**
     * 获取有效的姓名输入
     * @return 有效的姓名
     */
    private static String getValidName() {
        String name;
        while (true) {
            System.out.print("请输入姓名: ");
            name = scanner.nextLine().trim();

            if (name.isEmpty()) {
                System.out.println("姓名不能为空，请重新输入！");
                continue;
            }

            if (name.length() > 50) {
                System.out.println("姓名长度不能超过50个字符，请重新输入！");
                continue;
            }

            if (!name.matches("^[\\u4e00-\\u9fa5a-zA-Z\\s]+$")) {
                System.out.println("姓名只能包含中文、英文字母和空格，请重新输入！");
                continue;
            }

            break;
        }
        return name;
    }

    /**
     * 获取有效的年龄输入
     * @return 有效的年龄
     */
    private static int getValidAge() {
        int age;
        while (true) {
            System.out.print("请输入年龄: ");
            String input = scanner.nextLine().trim();

            if (input.isEmpty()) {
                System.out.println("年龄不能为空，请重新输入");
                continue;
            }

            try {
                age = Integer.parseInt(input);

                if (age < 0) {
                    System.out.println("年龄不能为负数，请重新输入");
                    continue;
                }

                if (age > 150) {
                    System.out.println("年龄不能超过150岁，请重新输入！");
                    continue;
                }

                break;

            } catch (NumberFormatException e) {
                System.out.println("请输入有效的数字，请重新输入！");
            }
        }
        return age;
    }
}