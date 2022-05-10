class Test {
    static fun f() -> int {
        ret 1
    }

    fun f1() -> int {
        ret 1
    }

    static fun g(int a,b,c) -> int {
        ret a + b + c
    }

    fun g1(int a, b, c) -> int {
        ret a + b + c
    }

    static fun h(int a;float b) -> void {
        int a1 = a
        float b1 = b
        println(a)
        println(b)
    }

    fun t() -> string {
        ret "string"
    }

    fun isTrue() -> bool {
        ret true
    }

    fun isFalse() -> bool {
        ret false
    }

    begin {

    }
}