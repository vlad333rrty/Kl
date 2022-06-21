import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

/**
 * @author vlad333rrty
 */
public class TestCodeGenerator {
    private static int index = 0;

    public static void main(String[] args) throws IOException {
        String program = "" +
                "    begin {\n" +
                "        int y = 0\n" +
                "        int x = y\n" +
                "        y = x\n" +
                "\n" +
                "        for int i = 0; i < 10; i = i + 2 {\n" +
                "            println(i)\n" +
                "        }\n" +
                "\n" +
                "        int a, b, c, d = 1, 2, 3, 4\n" +
                "        float g = 123.f\n" +
                "\n" +
                "        int[][][][][] arr\n" +
                "        double[][][][] heavy_arr = heavy()\n" +
                "    }\n" +
                "\n" +
                "    private static fun heavy() -> double[][][][] {\n" +
                "        double[][][][] res = new double[100][100][100][100]\n" +
                "        for int i = 0; i < 1000000; i = i + 1 {\n" +
                "             for int j = 0; j < 1000000; j = j + 1 {\n" +
                "                for int k = 0; k < 1000000; k = k + 1 {\n" +
                "                    for int l = 0; k < 1000000; l = l + 1 {\n" +
                "                        res[i][j][k][l] = 4\n" +
                "                    }\n" +
                "                }\n" +
                "             }\n" +
                "        }\n" +
                "        ret res\n" +
                "    }\n" +
                "}";
        int size = 50;
        int cur = 0;
        int requestedSize = 10000;
        try (BufferedWriter out = new BufferedWriter(new FileWriter("test7.ox"))) {
            while (cur < requestedSize) {
                String x = getClassDecl("Test8") + program;
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
