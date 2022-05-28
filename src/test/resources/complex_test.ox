class Test {
    begin {
        int len1, len2, len3 = 10, 9 ,8
        int[][][] a = new int[len1][len2][len3]
        for int i=0;i<len1;i=i+1{
            for int j=0;j<len2;j=j+1{
                for int k=0;k<len3;k=k+1{
                    a[i][j][k] = i + j + k
                }
            }
        }
        for int i=0;i<len1;i=i+1{
            print("( ")
            for int j=0;j<len2;j=j+1{
                print("[ ")
                for int k=0;k<len3;k=k+1{
                    print(a[i][j][k], ", ")
                }
                println("]")
            }
            println(")")
        }
    }
}
