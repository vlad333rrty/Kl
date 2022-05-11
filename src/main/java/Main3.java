

/**
 * @author vlad333rrty
 */
public class Main3 {
    public static void main(String[] args) {
        A b = new B();
    }
}


class A {
    int f() {
        return 1;
    }
}

class B extends A {
    int g() {
        return f();
    }
}