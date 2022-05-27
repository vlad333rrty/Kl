class Test {
    begin {
        int res = 0
        for int i=0;i<10;i=i+1{
            for int j=0;j<10;j=j+1 {
                for int k =0;k<10;k=k+1 {
                    res = res + 1
                }
            }
        }
        println(res)
    }
}