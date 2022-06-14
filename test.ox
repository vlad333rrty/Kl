class Test {
    begin {
        int x = 1 + 2 + 15
        int y = 24 - x
        int z = 24 * y - 3 + x
        if y > 10 {
            println(y - 10 + z)
        }
        println(z * z - y)

        if 24 > x + y -z {
            println(x + y - z)
        }
        float f = 1f

        println(x + y - f)
    }
}