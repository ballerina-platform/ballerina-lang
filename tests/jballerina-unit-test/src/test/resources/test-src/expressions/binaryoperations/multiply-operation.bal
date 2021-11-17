import ballerina/test;

function intMultiply(int a, int b) returns (int) {
    return a * b;
}

function overflowByMultiplication() {
    int num1 = -1;
    int num2 = -9223372036854775808;
    int ans = num1 * num2;
}

function floatMultiply(float a, float b) returns (float) {
    return a * b;
}

public const A = 10;
public const B = 20;
public const C = 30;
public const D = 40;

type SomeTypes A|B|C|D;

type E 12|13|14;

const float F = 20.5;
const float G = 10.5;

type H F|G;

type I 10.5|30.5;

const decimal J = 4.5;
const decimal K = 10.5;

type L J|K;

type IntType1 -2|-1|0|1|2;
type IntType2 int:Unsigned8|int:Signed32;
type IntType3 IntType1|IntType2;
type IntType4 IntType1|byte;

const float AA = 1.25;
const float BB = 2.5;

type FloatType1 -2.0f|-1.0f|0.0f|1.0f|2.0f;
type FloatType2 FloatType1;
type FloatType3 AA|BB;

const decimal CC = 1.25;
const decimal DD = 3.0;

type DecimalType1 CC|DD;
type DecimalType2 1d|2d|-1d|2d;
type DecimalType3 DecimalType1|DecimalType2;

function testMultiplicationWithTypes() {
    SomeTypes a1 = 10;
    int a2 = 20;
    SomeTypes a3 = 30;
    byte a4 = 25;
    int|int:Signed16 a5 = 15;
    E a6 = 12;
    float a7 = 10.5;
    H a8 = 10.5;
    I a9 = 30.5;
    L a10 = 10.5;
    decimal a11 = 20;

    assertEqual(a1 * a2, 200);
    assertEqual(a2 * a3, 600);
    assertEqual(a3 * a4, 750);
    assertEqual(a1 * a5, 150);
    assertEqual(a1 * a6, 120);
    assertEqual(a4 * a6, 300);
    assertEqual(a5 * a6, 180);
    assertEqual(a7 * a8, 110.25);
    assertEqual(a7 * a9, 320.25);
    assertEqual(a8 * a9, 320.25);
    assertEqual(a10 * a11, 210d);

    IntType3 a21 = 1;
    IntType3|int a22 = 2;
    int|IntType4 a23 = 3;

    test:assertEquals(a21 * a21, 1);
    test:assertEquals(a21 * a22, 2);
    test:assertEquals(a21 * a23, 3);
    test:assertEquals(a22 * a23, 6);
    test:assertEquals(a23 * a23, 9);

    FloatType2 a24 = -2;
    FloatType2 a25 = 1;
    FloatType3|float a26 = 1.25;
    float|FloatType3 a27 = 2.5;

    test:assertEquals(a24 * a24, 4.0);
    test:assertEquals(a24 * a25, -2.0);
    test:assertEquals(a24 * a26, -2.5);
    test:assertEquals(a24 * a27, -5.0);
    test:assertEquals(a25 * a25, 1.0);
    test:assertEquals(a25 * a26, 1.25);
    test:assertEquals(a25 * a27, 2.5);
    test:assertEquals(a26 * a26, 1.5625);
    test:assertEquals(a26 * a27, 3.125);
    test:assertEquals(a27 * a27, 6.25);

    DecimalType1 a28 = 1.25;
    DecimalType3|decimal a29 = 2;
    decimal|DecimalType3 a30 = 3;

    test:assertEquals(a28 * a28, 1.5625d);
    test:assertEquals(a28 * a29, 2.5d);
    test:assertEquals(a28 * a30, 3.75d);
    test:assertEquals(a29 * a29, 4d);
    test:assertEquals(a29 * a30, 6d);
    test:assertEquals(a30 * a30, 9d);
}

function testMultiplySingleton() {
    20 a1 = 20;
    int a2 = 2;
    20.5 a3 = 20.5;
    float a4 = 10;
    SomeTypes a5 = 30;
    int|int:Signed16 a6 = 5;
    E a7 = 12;

    assertEqual(a1 * a2, 40);
    assertEqual(a3 * a4, 205.0);
    assertEqual(a1 * a5, 600);
    assertEqual(a1 * a6, 100);
    assertEqual(a1 * a7, 240);
}

function testContextuallyExpectedTypeOfNumericLiteralInMultiply() {
    float a1 = 10.0 * 2;
    float a2 = 5 * 3 * 2.0;
    decimal a3 = 15.0 * 2;
    decimal a4 = 5.0 * 3.0 * 2;
    float? a5 = 10 * 5;
    decimal? a6 = 2 * 10.0;

    assertEqual(a1, 20.0);
    assertEqual(a2, 30.0);
    assertEqual(a3, 30.0d);
    assertEqual(a4, 30.0d);
    assertEqual(a5, 50.0);
    assertEqual(a6, 20.0d);
}

type Ints 1|2;
type T1 1|2|()|3;
type T2 1|2|3?;
type Decimals 1d|2d;

function testMultiplyNullable() {
    int? a1 = 10;
    int? a2 = 2;
    int? a3 = 1;
    int? a4 = ();
    int a5 = 5;
    float? a6 = 30.0;
    float? a7 = 10.0;
    float? a8 = ();
    float a9 = 5.0;

    int? a10 = (a1 * a2) * a5;
    int? a11 = a5 * a3;
    int? a12 = a4 * a1;
    float? a13 = a6 * a7;
    float? a14 = a6 * a9;
    float? a15 = a6 * a8;

    Ints a16 = 2;
    int? a17 = 1;
    int? a18 = a16 * a17;

    int a19 = 25;
    Ints? a20 = 2;

    T1 a21 = 2;
    T2? a22 = 1;
    ()|int a23 = ();
    T2? a24 = 1;

    Decimals? a25 = 1;
    Decimals? a26 = 2;

    int:Unsigned8 a = 1;
    int:Unsigned16 b = 2;
    int:Unsigned32 c = 5;
    int:Signed8 d = 20;
    int:Signed16 e = 10;
    int:Signed32 f = 10;
    byte g = 30;

    assertEqual(a10, 100);
    assertEqual(a11, 5);
    assertEqual(a12, ());
    assertEqual(a13, 300.0);
    assertEqual(a14, 150.0);
    assertEqual(a15, ());
    assertEqual(a18, 2);
    assertEqual(a19 * a20, 50);

    assertEqual(a21 * a21, 4);
    assertEqual(a21 * a22, 2);
    assertEqual(a21 * a23, ());
    assertEqual(a22 * a22, 1);
    assertEqual(a22 * a23, ());
    assertEqual(a23 * a23, ());
    assertEqual(a24 * a21, 2);
    assertEqual(a25 * a26, 2d);

    assertEqual(a * a, 1);
    assertEqual(a * b, 2);
    assertEqual(a * c, 5);
    assertEqual(a * d, 20);
    assertEqual(a * e, 10);
    assertEqual(a * f, 10);
    assertEqual(a * g, 30);

    assertEqual(b * c, 10);
    assertEqual(b * d, 40);
    assertEqual(b * e, 20);
    assertEqual(b * f, 20);
    assertEqual(b * g, 60);
    assertEqual(b * b, 4);

    assertEqual(c * c, 25);
    assertEqual(c * d, 100);
    assertEqual(c * e, 50);
    assertEqual(c * f, 50);
    assertEqual(c * g, 150);

    assertEqual(d * d, 400);
    assertEqual(d * e, 200);
    assertEqual(d * f, 200);
    assertEqual(d * g, 600);

    assertEqual(e * e, 100);
    assertEqual(e * f, 100);
    assertEqual(e * g, 300);

    assertEqual(f * f, 100);
    assertEqual(f * g, 300);

    assertEqual(g * g, 900);

    IntType3? a27 = 1;
    IntType3? a28 = 2;
    IntType4? a29 = 3;

    test:assertEquals(a27 * a27, 1);
    test:assertEquals(a27 * a28, 2);
    test:assertEquals(a27 * a29, 3);
    test:assertEquals(a28 * a29, 6);
    test:assertEquals(a29 * a29, 9);

    FloatType2? a30 = -2;
    FloatType2? a31 = 1;
    FloatType3? a32 = 1.25;
    FloatType3? a33 = 2.5;

    test:assertEquals(a30 * a30, 4.0);
    test:assertEquals(a30 * a31, -2.0);
    test:assertEquals(a30 * a32, -2.5);
    test:assertEquals(a30 * a33, -5.0);
    test:assertEquals(a31 * a31, 1.0);
    test:assertEquals(a31 * a32, 1.25);
    test:assertEquals(a31 * a33, 2.5);
    test:assertEquals(a32 * a32, 1.5625);
    test:assertEquals(a32 * a33, 3.125);
    test:assertEquals(a33 * a33, 6.25);

    DecimalType1? a34 = 1.25;
    DecimalType3? a35 = 2;
    DecimalType3? a36 = 3;

    test:assertEquals(a34 * a34, 1.5625d);
    test:assertEquals(a34 * a35, 2.5d);
    test:assertEquals(a34 * a36, 3.75d);
    test:assertEquals(a35 * a35, 4d);
    test:assertEquals(a35 * a36, 6d);
    test:assertEquals(a36 * a36, 9d);
}

function assertEqual(any actual, any expected) {
    if actual is anydata && expected is anydata && actual == expected {
        return;
    }

    if actual === expected {
        return;
    }

    string actualValAsString = actual.toString();
    string expectedValAsString = expected.toString();
    panic error(string `Assertion error: expected ${expectedValAsString} found ${actualValAsString}`);
}
