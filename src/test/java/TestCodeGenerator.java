import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

/**
 * @author vlad333rrty
 */
public class TestCodeGenerator {
    private static int index = 0;

    public static void main(String[] args) throws IOException {
        String program =
                "    begin {\n" +
                "        int len = 10\n" +
                "        int[] a = new int[len]\n" +
                "        for int i = 0; i < len; i = i + 1 {\n" +
                "            a[i] = -i\n" +
                "        }\n" +
                "        int[] result = sort(a, len)\n" +
                "        for int i = 0; i < len(result); i = i + 1 {\n" +
                "            print(result[i])\n" +
                "        }\n" +
                "    }\n" +
                "\n" +
                "    static fun sort(int[] a; int len) -> int[] {\n" +
                "        int j = 0\n" +
                "        int m = 10\n" +
                "        int[] count = new int[m]\n" +
                "        for ;j < len; j = j + 1 {\n" +
                "            int k = key(a[j])\n" +
                "            count[k] = count[k] + 1\n" +
                "        }\n" +
                "        int i = 1\n" +
                "        for ; i < m; i = i + 1 {\n" +
                "            count[i] = count[i] + count[i - 1]\n" +
                "        }\n" +
                "        int[] result = new int[len]\n" +
                "        for j = len - 1;j >= 0;j = j - 1 {\n" +
                "            int k = key(a[j])\n" +
                "            i = count[k] - 1\n" +
                "            count[k] = i\n" +
                "            result[i] = a[j]\n" +
                "        }\n" +
                "        ret result\n" +
                "    }\n" +
                "\n" +
                "    static fun key(int x) -> int {\n" +
                "        if x < 0 {\n" +
                "            x = -x\n" +
                "        }\n" +
                "        int t = x\n" +
                "        for ;t>0; {\n" +
                "            t = t / 10\n" +
                "            if t > 0 {\n" +
                "                x = t\n" +
                "            }\n" +
                "        }\n" +
                "        ret x\n" +
                "    }\n" +
                "\n" +
                "}\n";
        int size = 50;
        int cur = 0;
        int requestedSize = 300000;
        try (BufferedWriter out = new BufferedWriter(new FileWriter("test.ox"))) {
            while (cur < requestedSize) {
                String x = getClassDecl("Test") + program;
                out.write(x);
                cur += size;
            }
        }
        System.out.println(cur);
    }

    private static String getClassDecl(String name) {
        return String.format("class %s {\n", getNextUniq(name));
    }

    private static String getNextUniq(String name) {
        return name + index++;
    }

}
