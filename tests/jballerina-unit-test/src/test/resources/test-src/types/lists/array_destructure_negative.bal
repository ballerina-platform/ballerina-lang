public function testSimpleListBindingPattern() {
    int[4] intArray = [1, 2, 3, 4];

    int a = 0;
    int b = 0;
    int c = 0;
    int d = 0;
    int e = 0;
    boolean f = false;

    // Invalid size in binding pattern for simple types.
    [a, b, c, d, e] = intArray;

    // Invalid type in binding pattern for simple types.
    [a, b, c, f] = intArray;

    if (a != 1 || b != 2 || c != 3 || d != 4) {
        panic error("Simple List binding pattern didn't work");
    }
}
