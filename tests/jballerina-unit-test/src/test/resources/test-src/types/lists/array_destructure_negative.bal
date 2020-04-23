type Foo record {
    int a;
    string b;
};

type Bar record {
    boolean a;
};

public function testSimpleListBindingPattern() {
    int[4] intArray = [1, 2, 3, 4];
    int[] intArrayUnsealed = [1, 2, 3, 4];
    Foo[2] fooArray = [{a : 1, b : "1"}, {a : 2, b : "2"}];

    int a = 0;
    int b = 0;
    int c = 0;
    int d = 0;
    int e = 0;
    boolean f = false;
    int[] g;
    Foo h;
    Bar i;
    Bar j;

    // Invalid size in binding pattern for simple types.
    [a, b, c, d, e] = intArray;

    // Invalid type in binding pattern for simple types.
    [a, b, c, f] = intArray;

    // Invalid unsealed tuple binding pattern.
    [a, ...g] = intArrayUnsealed;

    // Invalid size for reference type.
    [h] = fooArray;

    // Type not compatible test for reference types.
    [i, j] = fooArray;

    if (a != 1 || b != 2 || c != 3 || d != 4) {
        panic error("Simple List binding pattern didn't work");
    }
}
