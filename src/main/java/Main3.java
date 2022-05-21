

/**
 * @author vlad333rrty
 */
public class Main3 {
    public static int aa = 0;
    private final int w = 3;
    private final B bbb = new B();

    public static void main(String[] args) {
        A b = new B();
        getA().f();
        B bb = new B();
        int a = bb.a;
        int r = B.b;
    }

    private static A getA() {
        return new B();
    }

    private void x() {
        int r = w;
    }
}


abstract class A {
    int f() {
        return 1;
    }

    abstract void s();
}

class B extends A {
    int a = 2;
    static int b = 3;

    int g() {
        return f();
    }

    @Override
    void s() {
        int x = 0;
    }
}