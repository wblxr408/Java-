import java.util.*;

public class SortBenchmark {
    private static final int SIZE = 10000;
    private static final int MAX_VALUE = 10000;
    private static final int SEED = 42;

    public static void main(String[] args) {
        int[] baseArray = generateRandomArray(SIZE, MAX_VALUE, SEED);

        Map<String, Runnable> algorithms = new LinkedHashMap<>();
        algorithms.put("冒泡排序", () -> bubbleSort(copyArray(baseArray)));
        algorithms.put("选择排序", () -> selectionSort(copyArray(baseArray)));
        algorithms.put("插入排序", () -> insertionSort(copyArray(baseArray)));
        algorithms.put("归并排序", () -> mergeSort(copyArray(baseArray)));
        algorithms.put("快速排序", () -> quickSort(copyArray(baseArray), 0, SIZE - 1));
        algorithms.put("希尔排序", () -> shellSort(copyArray(baseArray)));
        algorithms.put("堆排序", () -> heapSort(copyArray(baseArray)));
        algorithms.put("计数排序", () -> countingSort(copyArray(baseArray), MAX_VALUE));
        algorithms.put("桶排序", () -> bucketSort(copyArray(baseArray), MAX_VALUE));
        algorithms.put("基数排序", () -> radixSort(copyArray(baseArray)));

        System.out.printf("%-10s | %-10s%n", "算法", "耗时(ms)");
        System.out.println("-------------------------");

        for (Map.Entry<String, Runnable> entry : algorithms.entrySet()) {
            long start = System.nanoTime();
            entry.getValue().run();
            long end = System.nanoTime();
            double ms = (end - start) / 1_000_000.0;
            System.out.printf("%-10s | %-10.3f ms%n", entry.getKey(), ms);

        }
    }


    private static int[] generateRandomArray(int size, int maxValue, int seed) {
        Random rand = new Random(seed);
        int[] arr = new int[size];
        for (int i = 0; i < size; i++) arr[i] = rand.nextInt(maxValue);
        return arr;
    }

    private static int[] copyArray(int[] arr) {
        return Arrays.copyOf(arr, arr.length);
    }

    //1. 冒泡排序
    private static void bubbleSort(int[] arr) {
        for (int i = 0; i < arr.length - 1; i++)
            for (int j = 0; j < arr.length - i - 1; j++)
                if (arr[j] > arr[j + 1]) {
                    int temp = arr[j];
                    arr[j] = arr[j + 1];
                    arr[j + 1] = temp;
                }
    }

    //2. 选择排序
    private static void selectionSort(int[] arr) {
        for (int i = 0; i < arr.length - 1; i++) {
            int minIdx = i;
            for (int j = i + 1; j < arr.length; j++)
                if (arr[j] < arr[minIdx]) minIdx = j;
            int temp = arr[i];
            arr[i] = arr[minIdx];
            arr[minIdx] = temp;
        }
    }

    // 3. 插入排序
    private static void insertionSort(int[] arr) {
        for (int i = 1; i < arr.length; i++) {
            int key = arr[i];
            int j = i - 1;
            while (j >= 0 && arr[j] > key) {
                arr[j + 1] = arr[j];
                j--;
            }
            arr[j + 1] = key;
        }
    }

    //4. 归并排序
    private static void mergeSort(int[] arr) {
        if (arr.length <= 1) return;
        mergeSortHelper(arr, 0, arr.length - 1);
    }

    private static void mergeSortHelper(int[] arr, int left, int right) {
        if (left >= right) return;
        int mid = (left + right) / 2;
        mergeSortHelper(arr, left, mid);
        mergeSortHelper(arr, mid + 1, right);
        merge(arr, left, mid, right);
    }

    private static void merge(int[] arr, int left, int mid, int right) {
        int[] temp = new int[right - left + 1];
        int i = left, j = mid + 1, k = 0;
        while (i <= mid && j <= right) temp[k++] = arr[i] <= arr[j] ? arr[i++] : arr[j++];
        while (i <= mid) temp[k++] = arr[i++];
        while (j <= right) temp[k++] = arr[j++];
        System.arraycopy(temp, 0, arr, left, temp.length);
    }

    // 5. 快速排序
    private static void quickSort(int[] arr, int low, int high) {
        if (low >= high) return;
        int pivot = arr[low + (high - low) / 2];
        int i = low, j = high;
        while (i <= j) {
            while (arr[i] < pivot) i++;
            while (arr[j] > pivot) j--;
            if (i <= j) {
                int temp = arr[i];
                arr[i] = arr[j];
                arr[j] = temp;
                i++;
                j--;
            }
        }
        if (low < j) quickSort(arr, low, j);
        if (i < high) quickSort(arr, i, high);
    }

    // 6. 希尔排序
    private static void shellSort(int[] arr) {
        for (int gap = arr.length / 2; gap > 0; gap /= 2)
            for (int i = gap; i < arr.length; i++) {
                int temp = arr[i];
                int j = i;
                while (j >= gap && arr[j - gap] > temp) {
                    arr[j] = arr[j - gap];
                    j -= gap;
                }
                arr[j] = temp;
            }
    }

    //7. 堆排序
    private static void heapSort(int[] arr) {
        int n = arr.length;
        for (int i = n / 2 - 1; i >= 0; i--) heapify(arr, n, i);
        for (int i = n - 1; i > 0; i--) {
            int temp = arr[0];
            arr[0] = arr[i];
            arr[i] = temp;
            heapify(arr, i, 0);
        }
    }

    private static void heapify(int[] arr, int n, int i) {
        int largest = i, left = 2 * i + 1, right = 2 * i + 2;
        if (left < n && arr[left] > arr[largest]) largest = left;
        if (right < n && arr[right] > arr[largest]) largest = right;
        if (largest != i) {
            int swap = arr[i];
            arr[i] = arr[largest];
            arr[largest] = swap;
            heapify(arr, n, largest);
        }
    }

    // 8. 计数排序
    private static void countingSort(int[] arr, int maxValue) {
        int[] count = new int[maxValue + 1];
        for (int num : arr) count[num]++;
        int index = 0;
        for (int i = 0; i < count.length; i++)
            while (count[i]-- > 0) arr[index++] = i;
    }

    // 9. 桶排序
    private static void bucketSort(int[] arr, int maxValue) {
        int bucketCount = 10;
        List<List<Integer>> buckets = new ArrayList<>(bucketCount);
        for (int i = 0; i < bucketCount; i++) buckets.add(new ArrayList<>());

        for (int num : arr) {
            int bucketIdx = num * bucketCount / (maxValue + 1);
            buckets.get(bucketIdx).add(num);
        }

        int idx = 0;
        for (List<Integer> bucket : buckets) {
            Collections.sort(bucket);
            for (int num : bucket) arr[idx++] = num;
        }
    }

    // 10. 基数排序
    private static void radixSort(int[] arr) {
        int max = Arrays.stream(arr).max().getAsInt();
        for (int exp = 1; max / exp > 0; exp *= 10) countingSortByDigit(arr, exp);
    }

    private static void countingSortByDigit(int[] arr, int exp) {
        int n = arr.length;
        int[] output = new int[n];
        int[] count = new int[10];
        for (int i = 0; i < n; i++) count[(arr[i] / exp) % 10]++;
        for (int i = 1; i < 10; i++) count[i] += count[i - 1];
        for (int i = n - 1; i >= 0; i--) {
            int digit = (arr[i] / exp) % 10;
            output[count[digit] - 1] = arr[i];
            count[digit]--;
        }
        System.arraycopy(output, 0, arr, 0, n);
    }
}
