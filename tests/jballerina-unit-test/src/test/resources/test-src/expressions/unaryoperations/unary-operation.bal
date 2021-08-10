function negativeIntTest() returns [int, int] {
    int x;
    int y;
    x = -5;
    y = -x;

    return [x,y];
}

function positiveIntTest() returns [int, int] {
    int x;
    int y;
    x = +5;
    y = +x;

    return [x,y];
}

function negativeFloatTest() returns [float, float] {
    float x;
    float y;
    x = -5.0;
    y = -x;

    return [x,y];
}

function positiveFloatTest() returns [float, float] {
    float x;
    float y;
    x = +5.0;
    y = +x;

    return [x,y];
}

function booleanNotTest() returns [boolean, boolean, boolean] {
    boolean x;
    boolean y;
    boolean z;
    x = false;
    y = !x;
    z = !false;

    return [x,y,z];
}

function unaryExprInIfConditionTest() returns (boolean) {
    boolean x;
    x = false;
    if(!x) {
        return true;
    } else {
        return false;
    }
}

function unaryNegationTest(int a, int b) returns (int) {
    return a-(-b);
}

function unaryPositiveNegationTest(int a) returns (int) {
    return +-a;
}

function complementOperator(int a) returns int {
    return ~a;
}

function testComplementOperator() {
    assertEquality(-1, complementOperator(0));
    assertEquality(-6, complementOperator(5));
    assertEquality(4, complementOperator(-5));

    byte a1 = 0;
    int b1 = ~a1;
    assertEquality(b1, -1);

    int:Unsigned8 a2 = 0;
    int b2 = ~a2;
    assertEquality(b2, -1);

    int:Unsigned16 a3 = 0;
    int b3 = ~a2;
    assertEquality(b3, -1);

    int:Unsigned32 a4 = 0;
    int b4 = ~a2;
    assertEquality(b4, -1);

    int:Signed8 a5 = 0;
    int b5 = ~a5;
    assertEquality(b5, -1);

    int:Signed16 a6 = 0;
    int b6 = ~a6;
    assertEquality(b6, -1);

    int:Signed32 a7 = 0;
    int b7 = ~a7;
    assertEquality(b7, -1);
}

function testUnaryOperationsWithIntSubtypes() {
    int:Unsigned8 x1 = 7;
    int y1 = ~x1;
    int:Unsigned8 x2 = +7;
    assertEquality(-8, y1);
    assertEquality(7, x2);

    int:Signed8 x3 = 7;
    int y2 = ~x3;
    int:Signed8 x4 = +7;
    int:Signed8 x5 = -7;
    assertEquality(-8, y2);
    assertEquality(7, x4);
    assertEquality(-7, x5);

    int:Unsigned16 x6 = 7;
    int y3 = ~x6;
    int:Unsigned16 x7 = +7;
    assertEquality(-8, y3);
    assertEquality(7, x7);

    int:Signed16 x8 = 7;
    int y4 = ~x8;
    int:Signed16 x9 = +7;
    int:Signed16 x10 = -7;
    assertEquality(-8, y4);
    assertEquality(7, x9);
    assertEquality(-7, x10);

    int:Unsigned32 x11 = 7;
    int y5 = ~x6;
    int:Unsigned32 x12 = +7;
    assertEquality(-8, y5);
    assertEquality(7, x12);

    int:Signed32 x13 = 7;
    int y6 = ~x13;
    int:Signed32 x14 = +7;
    int:Signed32 x15 = -7;
    assertEquality(-8, y6);
    assertEquality(7, x14);
    assertEquality(-7, x15);

    byte x16 = 7;
    int y7 = ~x16;
    byte x17 = +7;
    assertEquality(-8, y7);
    assertEquality(7, x17);
}

function assertEquality(anydata expected, anydata actual) {
    if expected == actual {
        return;
    }
    panic error("expected '" + expected.toString() + "', found '" + actual.toString() + "'");
}
