class Test {
    begin {
        int y = 0
        int x = y
        y = x

        for int i = 0; i < 10; i = i + 2 {
            println(i)
        }
    }
}