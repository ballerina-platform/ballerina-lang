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

type Circle distinct object {
    int x;
    int y;
    int r;
};

type Color [int, int, int];

class ColoredCircle {
    *Circle;
    Color color;

    function init(int x, int y, int r, Color color) {
        self.x = x;
        self.y = y;
        self.r = r;
        self.color = color;
    }
}

class ColoredCircleLookAlike {
    int x;
    int y;
    int r;
    Color color;

    function init(int x, int y, int r, Color color) {
        self.x = x;
        self.y = y;
        self.r = r;
        self.color = color;
    }
}

// Share distinct base class `Circle` with ColoredCircle
class ColoredCirclePlus {
    *Circle;
    Color color;
    boolean isPlus;

    function init(int x, int y, int r, Color color, boolean isPlus) {
        self.x = x;
        self.y = y;
        self.r = r;
        self.color = color;
        self.isPlus = isPlus;
    }
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

    ColoredCircle cl = new ColoredCircle(1, 1, 1, [244, 233, 200]);
    any cl0 = cl; // Avoid always evaluate to true static check.
    assertEquality(cl0 is Circle, true);

    ColoredCircleLookAlike lc = new ColoredCircleLookAlike(1, 1, 1, [244, 233, 200]);
    any lc0 = lc;
    assertEquality(lc0 is Circle, false);

    Circle brightC = new ColoredCircle(1, 1, 1, [244, 233, 200]);
    assertEquality(brightC.x, 1);
    assertEquality(brightC.y, 1);
    assertEquality(brightC.r, 1);


    ColoredCirclePlus ccp = new ColoredCirclePlus(2, 2, 2, [1, 1, 1], true);
    // This should be possible as `ColoredCirclePlus` and `ColoredCircle` have the same same set of type-ids from
    // it's base type, and does not have their own type-ids.
    ColoredCircle ccp_ = ccp;
    any ccp0 = ccp;
    assertEquality(ccp0 is Circle, true);
    assertEquality(ccp0 is ColoredCircle, true);
}

function assertEquality(any|error actual, any|error expected) {
    if expected is anydata && actual is anydata && expected == actual {
        return;
    }

    if expected === actual {
        return;
    }

    string expectedValAsString = expected is error ? expected.toString() : expected.toString();
    string actualValAsString = actual is error ? actual.toString() : actual.toString();
    panic error("AssertionError",
            message = "expected '" + expectedValAsString + "', found '" + actualValAsString + "'");
}
