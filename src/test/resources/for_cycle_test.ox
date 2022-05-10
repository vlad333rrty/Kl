class Test {
    begin {
        for int i = 0; i < 10; i = i + 1 {
           print(i)
        }

        int res = 10
        for ;res > 0; {
            res = res - 1
            print(res)
        }

        int i = 0
        for ;i < 10; i = i + 1 {
             print(i)
        }

        for i = 10; i > 0; i = i - 1 {
            print(i)
        }
    }
}