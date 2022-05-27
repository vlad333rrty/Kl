class Test {
    begin {
        int len = 10
        T t = new T()
        A a = new A()
        int r = a.len(len)
        if t.getA().len(len) == r {
            println("success")
        }
        int i = 0
        int x = i
        x = 10

        if x == r {
            println("success")
        }
    }
}

class T {
    fun getA() -> A {
        ret new A()
    }
}

class A {
    fun len(int len) -> int {
        int i = 0
        for ; i < len; i = i + 1 {
        }
        ret i
    }
}