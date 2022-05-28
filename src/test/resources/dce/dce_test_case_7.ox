class Test {
    begin {
        int[] a = new int[10]
        for int i = 0; i < 10; i = i + 1 {
            a[i] = i
        }
        int res = binarySearch(a, 10, 4)
        println(res)
    }

    static fun binarySearch(int[] a; int len, x) -> int {
        int l, r = -1, len
        for ;l != r - 1; {
            int m = (l + r) / 2
            if a[m] > x {
                r = m
            } else {
                l = m
            }
        }
        ret a[l]
    }
}