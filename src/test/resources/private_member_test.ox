class Test {
    begin {
        T t = new T()
        t.f()
    }
}

class T {
    private fun f() -> int {
        ret 1
    }
}