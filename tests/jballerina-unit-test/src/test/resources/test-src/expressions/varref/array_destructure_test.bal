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

type Foo record {
    int a;
    string b;
};

type Bar record {
    int a;
};

public function testReferenceListBindingPattern() {
    Foo[2] fooArray = [{a : 1, b : "1"}, {a: 2, b : "2"}];

    Foo a;
    Foo b;

    [a, b] = fooArray;

    if (a.a != 1 || a.b != "1" || b.a != 2 || b.b != "2") {
        panic error("Reference list binding pattern error");
    }
}

public function testReferenceListBindingPatternWithUndefinedSize() {
    Foo[] fooArray = [{a : 1, b : "1"}, {a: 2, b : "2"}];

    Foo[] a;

    [...a] = fooArray;
    if (a[0].a != 1 || a[0].b != "1" || a[1].a != 2 || a[1].b != "2") {
        panic error("Reference list binding pattern error");
    }

}

public function testReferenceListBindingPatternWithRecordDestructure() {
    Foo[2] fooArray = [{a : 1, b : "1"}, {a: 2, b : "2"}];

    Foo a;
    int b;
    string c;
    map<anydata|error> d;

    [{a : b, b : c, ...d}, a] = fooArray;

    if (b != 1 || c != "1" || a.a != 2 || a.b != "2") {
        panic error("Reference list binding pattern error with record destructure");
    }
}

public function testReferenceListBindingPatternForUndefinedSizeWithDifferentType() {
    Foo[] fooArray = [{a : 1, b : "1"}, {a: 2, b : "2"}];

    Bar[] a;

    [...a] = fooArray;

    if (a[0].a != 1  || a[1].a != 2 ) {
        panic error("Reference list binding pattern error with undefined size and different type failed");
    }
}

public function testReferenceListBindingPatternWithTuples() {
    [int, int][3] tupleArray = [[1, 1], [2, 2], [3, 3]];

    [int, int] a;
    [int, int] b;
    int c;
    int d;

    [a, b, [c, d]] = tupleArray;

    if (a[0] != 1  || a[1] != 1 || b[0] != 2 || b[1] != 2 || c != 3 || d != 3) {
        panic error("Reference list binding pattern error with tuples nested");
    }
}
