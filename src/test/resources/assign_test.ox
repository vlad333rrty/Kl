class Test {
    begin {
        int i1, i2, i3, i4, i5, i6 = 1, 2, 3, 4, 5, 6
        println(i1, i2, i3, i4, i5, i6)

        long l1 = 123L
        println(l1)

        string s1, s2= "123", "abc"
        println(s1, s2)

        bool b1, b2 = true, false
        println(b1, b2)

        float f1, f2, f3 = 1.23f, 0.f, 23f
        println(f1, f2, f3)

        double d1, d2, d3, d4 = 1., 1.d, 2.2d, 6d
        println(d1, d2, d3, d4)

        int[] ia1, ia2 = new int[4], new int[1 + 4 - 3]
        float[] fa1, fa2 = new float[123], new float[345]
        string[] sa1 = new string[8]
        long[] la1 = new long[16]
        double[] da1 = new double[8]
    }
}