class Test {
    begin {
        T t = new T()
        int x = t.f(1, 2, 3, 7)
        println(x)
        long z = 4L
        println(x)
    }
}


class T {
    fun f(int a, b, c, threshold) -> int {
        int i = 0
        for ;i < threshold; {
            i = i + a + b + c
        }
        ret i
    }
}