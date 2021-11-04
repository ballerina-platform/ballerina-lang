import ballerina/test;

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

function testUnaryOperationsWithNonBasicTypes() {
    int|float x1 = +5;
    int|float x2 = ++5;
    int|float x3 = -5;
    int|float x4 = --5;
    int|decimal x5 = ~5;
    int|float|string x6 = +~5;
    anydata x7 = ++5;
    anydata x8 = --5;
    anydata x9 = +-5;
    anydata x10 = +~5;
    anydata x11 = ~-5;
    anydata x12 = -~5;

    test:assertEquals(x1, 5);
    test:assertEquals(x2, 5);
    test:assertEquals(x3, -5);
    test:assertEquals(x4, 5);
    test:assertEquals(x5, -6);
    test:assertEquals(x6, -6);
    test:assertEquals(x7, 5);
    test:assertEquals(x8, 5);
    test:assertEquals(x9, -5);
    test:assertEquals(x10, -6);
    test:assertEquals(x11, 4);
    test:assertEquals(x12, 6);

    int i = 5;
    int? a1 = +i;
    int? a2 = ++i;
    int? a3 = ~i;
    int? a4 = +~i;
    anydata a5 = +i;
    int|float a6 = -i;

    float f = -5.2;
    float? a7 = +f;
    anydata a8 = -f;

    decimal d = 7.45;
    decimal? a9 = +d;
    anydata a10 = -d;
    decimal|int a11 = +-d;
    anydata a12 = ++d;

    int:Signed16 s = 16;
    anydata a13 = ++s;
    int? a14 = +-s;
    int|decimal a15 = +~s;

    test:assertEquals(a1, 5);
    test:assertEquals(a2, 5);
    test:assertEquals(a3, -6);
    test:assertEquals(a4, -6);
    test:assertEquals(a5, 5);
    test:assertEquals(a6, -5);
    test:assertEquals(a7, -5.2);
    test:assertEquals(a8, 5.2);
    test:assertEquals(a9, 7.45d);
    test:assertEquals(a10, -7.45d);
    test:assertEquals(a11, -7.45d);
    test:assertEquals(a12, 7.45d);
    test:assertEquals(a13, 16);
    test:assertEquals(a14, -16);
    test:assertEquals(a15, -17);

    int:Signed8|int:Unsigned32 a16 = 12;
    int a17 = -a16;

    int:Unsigned8|int:Signed8 a18 = 10;
    int a19 = +a18;

    test:assertEquals(a17, -12);
    test:assertEquals(a19, 10);
}

type Ints -2|-1|0|1|2;
type Floats -2.0|-1.0|0.0|1.0|2.0;
type Decimals -2.0d|-1.0d|0d|1.0d|2.0d;

function testUnaryOperationsWithUserDefinedTypes() {
    Ints a = -2;
    Ints b = 1;
    Floats c = -2;
    Floats d = 0;
    Decimals e = -2;
    Decimals f = 1.0;

    int g = -a;
    int h = +b;
    float i = -c;
    float j = +d;
    decimal k = -e;
    decimal l = +f;

    assertEquality(g, 2);
    assertEquality(h, 1);
    assertEquality(i, 2.0);
    assertEquality(j, 0.0);
    assertEquality(k, 2d);
    assertEquality(l, 1.0d);
}

function testNullableUnaryExpressions() {
    int? a1 = 10;
    int? a2 = 5;
    int a3 = 2;
    float? a4 = 5.0;
    float? a5 = 15.0;
    float a6 = 4.0;

    int? a7 = +((a1 + a2) * a3);
    float? a8 = -((a4 + a5) / a6);
    int? a9 = ~a1;

    assertEquality(a7, 30);
    assertEquality(a8, -5.0);
    assertEquality(a9, -11);

    Ints? a = -2;
    Ints? b = 1;
    Floats? c = -2;
    Floats? d = 0;
    Decimals? e = -2;
    Decimals? f = 1.0;

    int? g = -a;
    int? h = +b;
    float? i = -c;
    float? j = +d;
    decimal? k = -e;
    decimal? l = +f;

    assertEquality(g, 2);
    assertEquality(h, 1);
    assertEquality(i, 2.0);
    assertEquality(j, 0.0);
    assertEquality(k, 2d);
    assertEquality(l, 1.0d);

    int:Signed8|int:Unsigned32? a16 = 12;
    int? a17 = -a16;

    int:Unsigned8|int:Signed8? a18 = 10;
    int? a19 = +a18;

    test:assertEquals(a17, -12);
    test:assertEquals(a19, 10);
}

function assertEquality(any actual, any expected) {
    if actual is anydata && expected is anydata && actual == expected {
        return;
    }
    panic error("expected '" + expected.toString() + "', found '" + actual.toString() + "'");
}
