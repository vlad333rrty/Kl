class Test {
    begin {
        int x = f(6)
        int z = 10
        for int i = x; i < z; i = i + f(z) {
            println(i)
        }
    }

    static fun f(int a) -> int {
        int x = 45
        int y, z = 4, 3
        ret y * z - a
    }
}