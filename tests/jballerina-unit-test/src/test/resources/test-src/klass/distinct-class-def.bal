distinct class Foo {
    int i = 0;
}

distinct class Bar {
    *Foo;

    function init(int i) {
        self.i = i;
    }
}

distinct class A {
    *B;

    function init(int i, int j) {
        self.i = i;
        self.j = j;
    }
}

distinct class B {
    int i = 0;
    int j = 0;
}

distinct class Y {
    *X;

    function init(int i) {
        self.i = i;
    }
}

class X {
    int i;

    function init(int i) {
        self.i = i;
    }
}

class Baz {
    *Foo;

    function init(int i) {
        self.i = i;
    }
}

class Claz {

}

function testDistinctAssignability() {
    Bar b = new(1);
    Foo f = b; // Can assign value of distinct subtype to a super type.
    assertEquality(f.i, 1);

    A x = new(11, 22);
    B y = x;
    assertEquality(y.i, 11);
    assertEquality(y.j, 22);

    Y w = new(33);
    X z = w; // Can assign value of distinct subtype to a non distinct super type.
    assertEquality(z.i, 33);
}

function testDistinctTypeIsType() {
    Bar b = new(1);
    Claz c = b;
    assertEquality(c is Bar, true);
    assertEquality(c is Foo, true);
    assertEquality(c is Baz, true);

    Baz z = new(1);
    assertEquality(z is Bar, false);
}

function assertEquality(any|error actual, any|error expected) {
    if expected is anydata && actual is anydata && expected == actual {
        return;
    }

    if expected === actual {
        return;
    }

    panic error("AssertionError",
            message = "expected '" + expected.toString() + "', found '" + actual.toString () + "'");
}
