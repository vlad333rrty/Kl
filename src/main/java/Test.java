/**
 * @author vlad333rrty
 */
public class Test {
    public static void main(String[] args) {
        byte y = 0;
        int x = y / 4;

        for(int i = 0; i < 10; i += 2) {
            System.out.print(i);
            System.out.print("\n");
        }

        byte a = 1;
        byte b = 2;
        byte c = 3;
        byte d = 4;
        float g = 123.0F / (float)a;
        double[][][][] heavy_arr = heavy();
        d = 4;
        d = 4;
        d = 4;
        d = 4;
        d = 4;
        d = 4;
        d = 4;
        d = 4;
        d = 4;

    }

    private static double[][][][] heavy() {
        double[][][][] res = new double[100][100][100][100];

        for(int i = 0; i < 100; ++i) {
            for(int j = 0; j < 100; ++j) {
                for(int k = 0; k < 100; ++k) {
                    for(int l = 0; l < 100; ++l) {
                        res[i][j][k][l] = (double)4;
                    }
                }
            }
        }

        return res;
    }
}
