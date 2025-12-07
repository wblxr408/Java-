import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Pattern;

public class SIS {

    //静态全局，全局放在最前面
    private static final AtomicInteger counter = new AtomicInteger(0);
    private static final int MAX = 20;
    private static final Path GRADE_FILE = Paths.get("grades.txt");
    private static final DateTimeFormatter TIME_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final List<GradeRecord> DEFAULT_GRADES = List.of(
            new GradeRecord("71108501", "张三", 80.0),
            new GradeRecord("71108502", "李四", 79.5),
            new GradeRecord("71108503", "王五", 91.0),
            new GradeRecord("71108504", "赵六", 60.0),
            new GradeRecord("71108505", "宋七", 18.6)
    );
    private final Scanner scanner = new Scanner(System.in);
    private final List<Student> students = new ArrayList<>();


    /** 一个内部记录类*/
    private record Student(int id, String name, String gender, int javaScore) {
        @Override
        public int javaScore() { return javaScore; }
        @Override
        public String toString() {
            return String.format("ID:%02d | 姓名:%-10s | 性别:%-6s | Java成绩:%3d",
                    id, name, gender, javaScore);
        }
    }

    /** 成绩文件记录实体。 */
    private record GradeRecord(String studentId, String name, double score) {
        @Override
        public String toString() {
            return String.format("学号:%s | 姓名:%-6s | 成绩:%5.1f",
                    studentId, name, score);
        }

        private String toDataLine() {
            return studentId + "," + name + "," + score;
        }
    }


    /** 添加学生信息的方法。 */
    private void inputStudent() {
        String name = inputName();
        String gender = inputGender();
        int score = inputScore();
        checkStudentCount();
        students.add(new Student(counter.incrementAndGet(), name, gender, score));
        System.out.println("添加成功！");
    }

    /** 以下是三个添加。 */
    private String inputName() {
        try {
            String name = getValidString("输入姓名: ");
            checkName(name);
            return name;
        } catch (Exception e) {
            System.out.println("输入无效: " + e.getMessage() + "，请重新输入。");
            return inputName();
        }
    }

    private String inputGender() {
        try {
            String gender = getValidString("输入性别(男/女): ");
            checkGender(gender);
            return gender;
        } catch (Exception e) {
            System.out.println("输入无效: " + e.getMessage() + "，请重新输入。");
            return inputGender();
        }
    }

    private int inputScore() {
        try {
            int score = getValidInt("输入 Java 成绩(0-100): ");
            checkScore(score);
            return score;
        } catch (Exception e) {
            System.out.println("输入无效: " + e.getMessage() + "，请重新输入。");
            return inputScore();
        }
    }

    /** 排名成绩 */
    private void rankStudents() {
        students.sort(Comparator.comparingInt(Student::javaScore).reversed());
        System.out.println(" 排名完成！");
        printStudents();
    }

    /** 打印学生信息。 */
    private void printStudents() {
        if (students.isEmpty())
            System.out.println("暂无学生信息。");
        else
            students.forEach(System.out::println);
    }

    /** 根据用户输入对指定目录的文件进行过滤和排序。 */
    private void sortFilesByLastModified() {
        System.out.println("\n===== 文件排序功能 =====");
        System.out.print("请输入目录路径: ");
        String directoryInput = scanner.nextLine().trim();
        System.out.print("请输入需要匹配的文件后缀(如 txt 或 java): ");
        String suffixInput = scanner.nextLine().trim();
        System.out.print("请输入排序方式(asc 表示升序，desc 表示降序): ");
        String orderInput = scanner.nextLine().trim().toLowerCase(Locale.ROOT);

        if (directoryInput.isEmpty() || suffixInput.isEmpty()) {
            System.out.println("目录和后缀名均不能为空。");
            return;
        }

        String normalizedSuffix = suffixInput.startsWith(".")
                ? suffixInput.substring(1)
                : suffixInput;
        if (normalizedSuffix.isEmpty()) {
            System.out.println("后缀名不能为空。");
            return;
        }
        File directory = new File(directoryInput);
        if (!directory.isDirectory()) {
            System.out.println("指定路径不是有效的目录。");
            return;
        }

        String regex = ".*\\." + Pattern.quote(normalizedSuffix) + "$";
        Pattern pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
        File[] files = directory.listFiles(file ->
                file.isFile() && pattern.matcher(file.getName()).matches());

        if (files == null || files.length == 0) {
            System.out.println("未找到匹配后缀的文件。");
            return;
        }

        Comparator<File> comparator = Comparator.comparingLong(File::lastModified);
        if ("desc".equals(orderInput)) {
            comparator = comparator.reversed();
        } else if (!"asc".equals(orderInput)) {
            System.out.println("未识别排序方式，默认采用升序。");
        }
        Arrays.sort(files, comparator);

        System.out.println("文件路径 | 最后修改时间");
        for (File file : files) {
            String formattedTime = TIME_FORMATTER.format(
                    Instant.ofEpochMilli(file.lastModified()).atZone(ZoneId.systemDefault()));
            System.out.printf("%s | %s%n", file.getAbsolutePath(), formattedTime);
        }
    }

    /** 成绩文件管理子菜单，用于读取和新增记录。 */
    private void manageGradeRecords() {
        ensureGradeFileInitialized();
        boolean exit = false;
        while (!exit) {
            System.out.println("""
                    \n===== 成绩记录管理 =====
                    1. 显示全部成绩
                    2. 添加新成绩
                    0. 返回主菜单
                    """);
            int choice = getValidInt("请选择操作 (0-2): ");
            scanner.nextLine();
            switch (choice) {
                case 1 -> displayGradeRecords();
                case 2 -> addGradeRecord();
                case 0 -> exit = true;
                default -> System.out.println("无效选项，请重新输入。");
            }
        }
    }

    /** 确保成绩文件存在并写入初始数据。 */
    private void ensureGradeFileInitialized() {
        try {
            if (Files.exists(GRADE_FILE) && Files.size(GRADE_FILE) > 0) {
                return;
            }
            List<String> lines = new ArrayList<>();
            for (GradeRecord record : DEFAULT_GRADES) {
                lines.add(record.toDataLine());
            }
            Files.write(GRADE_FILE, lines,
                    StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        } catch (IOException e) {
            System.out.println("初始化成绩文件失败: " + e.getMessage());
        }
    }

    /** 从文件中读取成绩记录。 */
    private List<GradeRecord> readGradeRecords() {
        ensureGradeFileInitialized();
        try {
            List<String> lines = Files.readAllLines(GRADE_FILE);
            List<GradeRecord> records = new ArrayList<>();
            for (String line : lines) {
                if (line.isBlank()) continue;
                String[] parts = line.split(",");
                if (parts.length != 3) {
                    System.out.println("跳过格式不正确的记录: " + line);
                    continue;
                }
                try {
                    double score = Double.parseDouble(parts[2].trim());
                    records.add(new GradeRecord(parts[0].trim(), parts[1].trim(), score));
                } catch (NumberFormatException e) {
                    System.out.println("跳过成绩格式异常的记录: " + line);
                }
            }
            return records;
        } catch (IOException e) {
            System.out.println("读取成绩文件失败: " + e.getMessage());
            return Collections.emptyList();
        }
    }

    /** 显示文件中的全部成绩记录。 */
    private void displayGradeRecords() {
        List<GradeRecord> records = readGradeRecords();
        if (records.isEmpty()) {
            System.out.println("暂无成绩记录。");
            return;
        }
        System.out.println("学号        | 姓名      | 成绩");
        records.forEach(System.out::println);
    }

    /** 添加新的成绩记录至文件。 */
    private void addGradeRecord() {
        String studentId = getValidString("输入学号: ");
        String name = getValidString("输入姓名: ");
        double score = getValidDouble("输入成绩(0-100，可含小数): ");
        scanner.nextLine();

        if (score < 0 || score > 100) {
            System.out.println("成绩必须在 0~100 之间。");
            return;
        }

        GradeRecord record = new GradeRecord(studentId, name, score);
        try {
            Files.writeString(GRADE_FILE, record.toDataLine() + System.lineSeparator(),
                    StandardOpenOption.CREATE, StandardOpenOption.APPEND);
            System.out.println("新成绩记录添加成功。");
            displayGradeRecords();
        } catch (IOException e) {
            System.out.println("写入成绩文件失败: " + e.getMessage());
        }
    }

    /** 欢迎界面。 */
    private void printWelcome(){
        System.out.println("""
                    \n===== 学生信息系统 SIS =====
                    1. 添加学生信息
                    2. 查看所有学生
                    3. 按成绩排名
                    4. 查看学生总数
                    5. 文件按修改时间排序
                    6. 成绩记录管理
                    0. 退出系统
                    """);
    }

    /** 判断是否退出。 */
    private boolean checkExit(){
        System.out.println("系统退出，再见！");
        return true;
    }
    /** 获取合法字符串输入。 */
    private String getValidString(String prompt) {
        System.out.print(prompt);
        return scanner.nextLine().trim();
    }

    /** 获取合法整数输入。 */
    private int getValidInt(String prompt) {
        while (true) {
            try {
                System.out.print(prompt);
                return scanner.nextInt();
            } catch (InputMismatchException e) {
                System.out.println("️ 输入无效！请输入整数。");
                scanner.next();
            }
        }
    }

    /** 检查学生总数是否超过上限 */
    private void checkStudentCount() {
        if (students.size() >= MAX)
            throw new IllegalStateException("学生数量已达上限");
    }

    /** 检查姓名是否合法 */
    private void checkName(String name) {
        if (!name.matches("[\\p{IsHan}a-zA-Z]+( [\\p{IsHan}a-zA-Z]+)*"))
            throw new IllegalArgumentException("姓名只能合理输入");
    }

    /** 检查性别是否合法 */
    private void checkGender(String gender) {
        if (!gender.equals("男") && !gender.equals("女"))
            throw new IllegalArgumentException("性别必须为“男”或“女”");
    }

    /** 检查Java成绩是否合法 */
    private void checkScore(int score) {
        if (score < 0 || score > 100)
            throw new IllegalArgumentException("成绩必须在 0~100 之间");
    }

    /** 程序开始的具体实现方法。 */
    private void run() {
        boolean exit=false;
        while (!exit) {
            printWelcome();
            int choice = getValidInt("请选择操作 (0-6): ");
            scanner.nextLine();
            switch (choice) {
                case 1 -> inputStudent();
                case 2 -> printStudents();
                case 3 -> rankStudents();
                case 4 -> System.out.println("当前学生总数: " + counter.get());
                case 5 -> sortFilesByLastModified();
                case 6 -> manageGradeRecords();
                case 0 -> exit=checkExit();
                default -> System.out.println("无效选项，请重新输入。");
            }
        }
    }

    /** 获取合法双精度输入。 */
    private double getValidDouble(String prompt) {
        while (true) {
            try {
                System.out.print(prompt);
                return scanner.nextDouble();
            } catch (InputMismatchException e) {
                System.out.println("? 输入无效！请输入数字。");
                scanner.next();
            }
        }
    }

    /** 程序入口接口。 */
    public void start() {
        run();
    }

    public static void main(String[] args) {
        new SIS().start();
    }
}