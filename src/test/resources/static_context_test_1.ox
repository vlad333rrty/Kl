class Test {
    fun f() -> float[][][] {
        ret new float[4][4][4]
    }

    begin {
        float[][][] fArray = f()
        println(fArray)
    }
}