import java.util.Arrays;

/**
 * @author vlad333rrty
 */
public class Main3 {
    public static void main(String[] args) {
        int len = 1000;
        Integer[] a = new Integer[len];
        for (int i = 0; i < len; i = i + 1) {
            a[i] = -i;
        }
        Arrays.sort(a);
        System.out.println(Arrays.toString(a));
    }
}
