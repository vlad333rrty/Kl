class IntStack {
    private int[] storage
    private int index

    fun init(int initSize) {
        storage = new int[initSize]
        index = 0
    }

    fun push(int value) {
        int len = len(storage)
        if index >= len {
            int[] storage2 = new int[len * 2]
            copy(storage, storage2, len)
            storage = storage2
        }
        storage[index] = value
        index = index + 1
    }

    fun pop() -> int {
        if index <= 0 {
            println("ERROR: stack is empty")
            ret 0
        }
        int value = storage[index - 1]
        index = index - 1
        ret value
    }

    fun size() -> int {
        ret index
    }

    private fun copy(int[] source, dest; int sourceSize) {
        for int i=0;i<sourceSize;i=i+1{
            dest[i] = source[i]
        }
    }
}

class Test {
    begin {
        IntStack stack = new IntStack()
        stack.init(4)
        stack.push(1)
        stack.push(2)
        stack.push(3)
        stack.push(4)
        for ;stack.size() > 0; {
            println(stack.pop())
        }
    }
}