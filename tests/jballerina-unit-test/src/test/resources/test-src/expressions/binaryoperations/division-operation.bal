function intDivide(int a, int b) returns (int) {
    return a / b;
}

function floatDivide(float a, float b) returns (float) {
    return a / b;
}

public function overflowByDivision() {
 int val = -1;
 int val1 = getPowerof(-2, 63);
 int k = val1/val;
}

function getPowerof(int num, int power) returns int {
    int i = 0;
    int res = 1;
    while (i < power) {
        res = res * num;
        i = i + 1;
    }
    return res;
}

public const A = 10;
public const B = 20;
public const C = 30;
public const D = 40;

type SomeTypes A|B|C|D;

type E 12|13|14;

const float F = 20.25;
const float G = 10.0;

type H F|G;

type I 10.5|10.0;

const decimal J = 4.0;
const decimal K = 5.0;

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

const decimal CC = 1.2;
const decimal DD = 3.0;

type DecimalType1 CC|DD;
type DecimalType2 1d|2d|-1d|2d;
type DecimalType3 DecimalType1|DecimalType2;

function testDivisionWithTypes() {
    SomeTypes a1 = 10;
    int a2 = 20;
    SomeTypes a3 = 30;
    byte a4 = 25;
    int|int:Signed16 a5 = 15;
    E a6 = 12;
    float a7 = 10.5;
    H a8 = 10.0;
    I a9 = 10.0;
    L a10 = 5.0;
    decimal a11 = 10.0;

    assertEqual(a1 / a2, 0);
    assertEqual(a2 / a3, 0);
    assertEqual(a3 / a4, 1);
    assertEqual(a1 / a5, 0);
    assertEqual(a1 / a6, 0);
    assertEqual(a4 / a6, 2);
    assertEqual(a5 / a6, 1);
    assertEqual(a7 / a8, 1.05);
    assertEqual(a7 / a9, 1.05);
    assertEqual(a8 / a9, 1.0);
    assertEqual(a11 / a10, 2d);

    IntType3 a21 = 1;
    int|IntType3 a22 = 2;
    IntType4|int a23 = 3;

    assertEqual(a21 / a21, 1);
    assertEqual(a21 / a22, 0);
    assertEqual(a21 / a23, 0);
    assertEqual(a22 / a23, 0);
    assertEqual(a23 / a23, 1);

    FloatType2 a24 = -2;
    FloatType2 a25 = 1;
    float|FloatType3 a26 = 1.25;
    FloatType3|float a27 = 2.5;

    assertEqual(a24 / a24, 1.0);
    assertEqual(a24 / a25, -2.0);
    assertEqual(a24 / a26, -1.6);
    assertEqual(a24 / a27, -0.8);
    assertEqual(a25 / a25, 1.0);
    assertEqual(a25 / a26, 0.8);
    assertEqual(a25 / a27, 0.4);
    assertEqual(a26 / a26, 1.0);
    assertEqual(a26 / a27, 0.5);
    assertEqual(a27 / a27, 1.0);

    DecimalType1 a28 = 1.2;
    DecimalType3|decimal a29 = 2;
    decimal|DecimalType3 a30 = 3;

    assertEqual(a28 / a28, 1d);
    assertEqual(a28 / a29, 0.6d);
    assertEqual(a28 / a30, 0.4d);
    assertEqual(a29 / a29, 1d);
    assertEqual(a29 / a30, 0.6666666666666666666666666666666667d);
    assertEqual(a30 / a30, 1d);
}

function testDivisionSingleton() {
    20 a1 = 20;
    int a2 = 2;
    20.5 a3 = 20.5;
    float a4 = 10;
    SomeTypes a5 = 30;
    int|int:Signed16 a6 = 5;
    E a7 = 12;

    assertEqual(a1 / a2, 10);
    assertEqual(a3 / a4, 2.05);
    assertEqual(a1 / a5, 0);
    assertEqual(a1 / a6, 4);
    assertEqual(a1 / a7, 1);
}

function testContextuallyExpectedTypeOfNumericLiteralInDivision() {
    float a1 = 10.0 / 2.0;
    float a2 = (10 / 5) / 2.0;
    decimal a3 = 30.0 / 15;
    decimal a4 = 9.0 / 3.0;
    float? a5 = 10 / 2;
    decimal a6 = 20 / 2.0;

    assertEqual(a1, 5.0);
    assertEqual(a2, 1.0);
    assertEqual(a3, 2.0d);
    assertEqual(a4, 3.0d);
    assertEqual(a5, 5.0);
    assertEqual(a6, 10.0d);
}

type Ints 1|2;
type T1 1|2|()|3;
type T2 1|2|3?;
type Decimals 1d|2d;

function testDivisionNullable() {
    int? a1 = 10;
    int? a2 = 2;
    int? a3 = 1;
    int? a4 = ();
    int a5 = 5;
    float? a6 = 30.0;
    float? a7 = 10.0;
    float? a8 = ();
    float a9 = 5.0;

    int? a10 = (a1 / a2) / a5;
    int? a11 = a5 / a3;
    int? a12 = a4 / a1;
    float? a13 = a6 / a7;
    float? a14 = a6 / a9;
    float? a15 = a6 / a8;

    Ints a16 = 2;
    int? a17 = 1;
    int? a18 = a16 / a17;

    int a19 = 30;
    Ints? a20 = 2;

    T1 a21 = 2;
    T2? a22 = 1;
    ()|int a23 = ();
    T2? a24 = 1;

    Decimals? a25 = 1;
    Decimals? a26 = 2;

    int:Unsigned32 a = 1000;
    int:Unsigned16 b = 500;
    int:Unsigned8 c = 200;
    int:Signed8 d = 100;
    int:Signed16 e = 50;
    int:Signed32 f = 10;
    byte g = 5;

    assertEqual(a10, 1);
    assertEqual(a11, 5);
    assertEqual(a12, ());
    assertEqual(a13, 3.0);
    assertEqual(a14, 6.0);
    assertEqual(a15, ());
    assertEqual(a16, 2);
    assertEqual(a19 / a20, 15);

    assertEqual(a21 / a21, 1);
    assertEqual(a21 / a22, 2);
    assertEqual(a21 / a23, ());
    assertEqual(a22 / a22, 1);
    assertEqual(a22 / a23, ());
    assertEqual(a23 / a23, ());
    assertEqual(a21 / a24, 2);
    assertEqual(a26 / a25, 2d);

    assertEqual(a / a, 1);
    assertEqual(a / b, 2);
    assertEqual(a / c, 5);
    assertEqual(a / d, 10);
    assertEqual(a / e, 20);
    assertEqual(a / f, 100);
    assertEqual(a / g, 200);

    assertEqual(b / c, 2);
    assertEqual(b / d, 5);
    assertEqual(b / e, 10);
    assertEqual(b / f, 50);
    assertEqual(b / g, 100);
    assertEqual(b / b, 1);

    assertEqual(c / c, 1);
    assertEqual(c / d, 2);
    assertEqual(c / e, 4);
    assertEqual(c / f, 20);
    assertEqual(c / g, 40);

    assertEqual(d / d, 1);
    assertEqual(d / e, 2);
    assertEqual(d / f, 10);
    assertEqual(d / g, 20);

    assertEqual(e / e, 1);
    assertEqual(e / f, 5);
    assertEqual(e / g, 10);

    assertEqual(f / f, 1);
    assertEqual(f / g, 2);

    assertEqual(g / g, 1);

    IntType3? a27 = 1;
    IntType3? a28 = 2;
    IntType4? a29 = 3;

    assertEqual(a27 / a27, 1);
    assertEqual(a27 / a28, 0);
    assertEqual(a27 / a29, 0);
    assertEqual(a28 / a29, 0);
    assertEqual(a29 / a29, 1);

    FloatType2? a30 = -2;
    FloatType2? a31 = 1;
    FloatType3? a32 = 1.25;
    FloatType3? a33 = 2.5;

    assertEqual(a30 / a30, 1.0);
    assertEqual(a30 / a31, -2.0);
    assertEqual(a30 / a32, -1.6);
    assertEqual(a30 / a33, -0.8);
    assertEqual(a31 / a31, 1.0);
    assertEqual(a31 / a32, 0.8);
    assertEqual(a31 / a33, 0.4);
    assertEqual(a32 / a32, 1.0);
    assertEqual(a32 / a33, 0.5);
    assertEqual(a33 / a33, 1.0);

    DecimalType1? a34 = 1.2;
    DecimalType3? a35 = 2;
    DecimalType3? a36 = 3;

    assertEqual(a34 / a34, 1d);
    assertEqual(a34 / a35, 0.6d);
    assertEqual(a34 / a36, 0.4d);
    assertEqual(a35 / a35, 1d);
    assertEqual(a35 / a36, 0.6666666666666666666666666666666667d);
    assertEqual(a36 / a36, 1d);
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
