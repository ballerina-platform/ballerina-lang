public function testSimpleListBindingPattern() {
    int[4] intArray = [1, 2, 3, 4];

    int a;
    int b;
    int c;
    int d;

    [a, b, c, d] = intArray;

    if (a != 1 || b != 2 || c != 3 || d != 4) {
        panic error("Simple List binding pattern didn't work");
    }
}
