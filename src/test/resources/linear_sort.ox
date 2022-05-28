class Test {
    begin {
        int len = 10
        int[] a = new int[len]
        for int i = 0; i < len; i = i + 1 {
            a[i] = -i
        }
        int[] result = sort(a, len)
        for int i = 0; i < len(result); i = i + 1 {
            print(result[i])
        }
    }

    static fun sort(int[] a; int len) -> int[] {
        int j = 0
        int m = 10
        int[] count = new int[m]
        for ;j < len; j = j + 1 {
            int k = key(a[j])
            count[k] = count[k] + 1
        }
        int i = 1
        for ; i < m; i = i + 1 {
            count[i] = count[i] + count[i - 1]
        }
        int[] result = new int[len]
        for j = len - 1;j >= 0;j = j - 1 {
            int k = key(a[j])
            i = count[k] - 1
            count[k] = i
            result[i] = a[j]
        }
        ret result
    }

    static fun key(int x) -> int {
        if x < 0 {
            x = -x
        }
        int t = x
        for ;t>0; {
            t = t / 10
            if t > 0 {
                x = t
            }
        }
        ret x
    }

}