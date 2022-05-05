/**
 * @author vlad333rrty
 */
public class Main3 {

    public static int x(int a, int b) {
        double g = 0.5D;

        do {
            g -= 0.1D;
            System.out.print(g);
            System.out.print(" ");
            System.out.print("\n");
        } while(!(g <= 0));

        System.out.print(g);
        System.out.print(" ");
        System.out.print("\n");
        return 1;
    }

    public static void main(String[] args) {
        int res = x(1, 2);
        System.out.print(res);
        System.out.print(" ");
        System.out.print("\n");
        res = 0;

        for(int i = 0; i < 10; ++i) {
            ++res;
        }

        System.out.print(res);
        System.out.print(" ");
        System.out.print("\n");
    }
}
