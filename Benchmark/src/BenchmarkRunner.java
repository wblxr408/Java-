import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.Random;

/**
 * 排序算法性能测试主程序
 * 包含所有算法实现和测试逻辑
 */
public class BenchmarkRunner {

    // 数据规模配置
    private static final int[] DATA_SIZES = {
            1000, 10000, 100000, 1000000, 10000000,100000000
    };

    public static void main(String[] args) {
        testAll();
        generateCSVData();

    }

    /**
     * 测试集合函数
     */
    private static void testAll() {
        // 测试所有算法
        testAlgorithm("快速排序 (QuickSort)", BenchmarkRunner::quickSort);
        testAlgorithm("归并排序 (MergeSort)", BenchmarkRunner::mergeSort);
        testAlgorithm("堆排序 (HeapSort)", BenchmarkRunner::heapSort);
        testAlgorithm("希尔排序 (ShellSort)", BenchmarkRunner::shellSort);
        testAlgorithm("Java Arrays.sort (基线方法)", Arrays::sort);
    }
    /**
     * 测试单个排序算法
     */
    private static void testAlgorithm(String name, SortAlgorithm algorithm) {
        System.out.println("算法: " + name);
        System.out.printf("%-15s %-15s %-15s%n", "数据规模", "时间(ms)", "验证结果");

        for (int size : DATA_SIZES) {
            int[] data = generateRandomData(size);
            long time = timeSort(data, algorithm);
            boolean valid = validate(data);
            System.out.printf("%-15d %-15d %-15s%n", size, time, valid ? " 正确" : " 错误");
        }
        System.out.println("\n");
    }

    /**
     * 生成CSV数据文件
     */
    private static void generateCSVData() {
        try (PrintWriter writer = new PrintWriter(new FileWriter("benchmark_results.csv"))) {
            // 写入表头
            writer.println("database,QuickSort,MergeSort,HeapSort,ShellSort,Java Arrays.sort");

            // 写入数据
            for (int size : DATA_SIZES) {
                writer.print(size);

                // 快速排序
                int[] data1 = generateRandomData(size);
                writer.print("," + timeSort(data1, BenchmarkRunner::quickSort));

                // 归并排序
                int[] data2 = generateRandomData(size);
                writer.print("," + timeSort(data2, BenchmarkRunner::mergeSort));

                // 堆排序
                int[] data3 = generateRandomData(size);
                writer.print("," + timeSort(data3, BenchmarkRunner::heapSort));

                // 希尔排序
                int[] data4 = generateRandomData(size);
                writer.print("," + timeSort(data4, BenchmarkRunner::shellSort));

                // Java排序
                int[] data5 = generateRandomData(size);
                writer.print("," + timeSort(data5, Arrays::sort));

                writer.println();
            }

        } catch (IOException e) {
            System.err.println("生成CSV文件时出错: " + e.getMessage());
        }
    }

    /**
     * 统计排序时间
     */
    private static long timeSort(int[] data, SortAlgorithm algorithm) {
        long start = System.currentTimeMillis();
        algorithm.sort(data);
        return System.currentTimeMillis() - start;
    }

    /**
     * 生成随机数据
     */
    private static int[] generateRandomData(int size) {
        int[] data = new int[size];
        Random random = new Random(42);
        for (int i = 0; i < size; i++) {
            data[i] = random.nextInt(size * 10);
        }
        return data;
    }

    /**
     * 验证排序结果
     */
    private static boolean validate(int[] data) {
        for (int i = 0; i < data.length - 1; i++) {
            if (data[i] > data[i + 1]) return false;
        }
        return true;
    }

    /**
     * 快速排序
     */
    private static void quickSort(int[] arr) {
        quickSortHelper(arr, 0, arr.length - 1);
    }

    private static void quickSortHelper(int[] arr, int low, int high) {
        if (low < high) {
            int pi = partition(arr, low, high);
            quickSortHelper(arr, low, pi - 1);
            quickSortHelper(arr, pi + 1, high);
        }
    }

    private static int partition(int[] arr, int low, int high) {
        int mid = low + (high - low) / 2;
        if (arr[mid] < arr[low]) swap(arr, low, mid);
        if (arr[high] < arr[low]) swap(arr, low, high);
        if (arr[mid] < arr[high]) swap(arr, mid, high);

        int pivot = arr[high];
        int i = low - 1;
        for (int j = low; j < high; j++) {
            if (arr[j] <= pivot) {
                i++;
                swap(arr, i, j);
            }
        }
        swap(arr, i + 1, high);
        return i + 1;
    }

    /**
     * 归并排序
     */
    private static void mergeSort(int[] arr) {
        mergeSortHelper(arr, 0, arr.length - 1);
    }

    private static void mergeSortHelper(int[] arr, int left, int right) {
        if (left < right) {
            int mid = left + (right - left) / 2;
            mergeSortHelper(arr, left, mid);
            mergeSortHelper(arr, mid + 1, right);
            merge(arr, left, mid, right);
        }
    }

    private static void merge(int[] arr, int left, int mid, int right) {
        int n1 = mid - left + 1;
        int n2 = right - mid;
        int[] L = new int[n1];
        int[] R = new int[n2];

        System.arraycopy(arr, left, L, 0, n1);
        System.arraycopy(arr, mid + 1, R, 0, n2);

        int i = 0, j = 0, k = left;
        while (i < n1 && j < n2) {
            arr[k++] = (L[i] <= R[j]) ? L[i++] : R[j++];
        }
        while (i < n1) arr[k++] = L[i++];
        while (j < n2) arr[k++] = R[j++];
    }

    /**
     * 堆排序
     */
    private static void heapSort(int[] arr) {
        int n = arr.length;
        for (int i = n / 2 - 1; i >= 0; i--) {
            heapify(arr, n, i);
        }
        for (int i = n - 1; i > 0; i--) {
            swap(arr, 0, i);
            heapify(arr, i, 0);
        }
    }

    private static void heapify(int[] arr, int n, int i) {
        int largest = i;
        int left = 2 * i + 1;
        int right = 2 * i + 2;

        if (left < n && arr[left] > arr[largest]) largest = left;
        if (right < n && arr[right] > arr[largest]) largest = right;

        if (largest != i) {
            swap(arr, i, largest);
            heapify(arr, n, largest);
        }
    }

    /**
     * 希尔排序
     */
    private static void shellSort(int[] arr) {
        int n = arr.length;
        int gap = 1;
        while (gap < n / 3) gap = 3 * gap + 1;

        while (gap >= 1) {
            for (int i = gap; i < n; i++) {
                int temp = arr[i];
                int j = i;
                while (j >= gap && arr[j - gap] > temp) {
                    arr[j] = arr[j - gap];
                    j -= gap;
                }
                arr[j] = temp;
            }
            gap /= 3;
        }
    }

    /**
     * 交换数组元素
     */
    private static void swap(int[] arr, int i, int j) {
        int temp = arr[i];
        arr[i] = arr[j];
        arr[j] = temp;
    }

    /**
     * 排序算法函数式接口
     */
    @FunctionalInterface
    interface SortAlgorithm {
        void sort(int[] arr);
    }
}