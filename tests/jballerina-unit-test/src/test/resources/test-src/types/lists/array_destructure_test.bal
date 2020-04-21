public function testSimpleListBindingPattern() {
    int[4] intArray = [1, 2, 3, 4];

    int a = 0;
    int b = 0;
    int c = 0;
    int d = 0;

    [a, b, c, d] = intArray;

    if (a != 1 || b != 2 || c != 3 || d != 4) {
        panic error("Simple List binding pattern didn't work");
    }
}

public function testSimpleListBindingPatternWithUndefinedSize() {
    int[] intArray = [1, 2, 3, 4];

    int[] a;

    [...a] = intArray;

    if (a[0] != 1 || a[1] != 2 || a[2] != 3 || a[3] != 4) {
        panic error("Simple List binding pattern with undefined size didn't work");
    }
}
