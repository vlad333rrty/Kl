class Test {
    private static int[][] a
    begin {
        int l = 0
        int k = 12 - 2
        a = new int[10][k]
        for int i = 0; i < k; i = i + 1 {
            a[l][i] = i
        }
        for int i=0;i<10;i=i+1 {
            for int j=0;j<k;j=j+1{
                println(a[i][j])
            }
        }
    }
}