class Test {
    begin {
        int len = 1000
        int[] a = new int[len]
        for int i = 0; i < len; i = i + 1 {
            a[i] = -i
        }
        sort(a, len)
        for int i = 0; i < len; i = i + 1 {
            print(a[i])
            if i < len - 1 {
                print(", ")
            }
        }
    }

    static fun sort(int[] a;int len) {
        int rBound = len
        for ;rBound > 1; {
            int last = 0
            for int i = 1; i < rBound; i = i + 1 {
                if a[i - 1] > a[i] {
                    a[i - 1], a[i] = a[i], a[i - 1]
                    last = i
                }
            }
            rBound = last
        }
    }
}