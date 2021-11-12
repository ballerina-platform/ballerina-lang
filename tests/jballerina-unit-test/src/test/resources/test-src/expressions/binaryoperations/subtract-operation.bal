function intSubtract(int a, int b) returns (int) {
    return a - b;
}

function overflowBySubtraction() {
    int num1 = -9223372036854775808;
    int num2 = 1;
    int ans = num1 - num2;
}

function floatSubtract(float a, float b) returns (float) {
    return a - b;
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

function testSubtractionWithTypes() {
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
    decimal a11 = 5.5;

    assertEqual(a2 - a1, 10);
    assertEqual(a3 - a2, 10);
    assertEqual(a3 - a4, 5);
    assertEqual(a1 - a5, -5);
    assertEqual(a1 - a6, -2);
    assertEqual(a4 - a6, 13);
    assertEqual(a5 - a6, 3);
    assertEqual(a7 - a8, 0.0);
    assertEqual(a7 - a9, -20.0);
    assertEqual(a8 - a9, -20.0);
    assertEqual(a10 - a11, 5d);

    IntType3 a21 = 1;
    IntType3 a22 = 255;
    IntType4 a23 = 127;

    assertEqual(a21 - a21, 0);
    assertEqual(a21 - a22, -254);
    assertEqual(a21 - a23, -126);
    assertEqual(a22 - a23, 128);
    assertEqual(a23 - a23, 0);

    FloatType2 a24 = -2;
    FloatType2 a25 = 1;
    FloatType3 a26 = 1.25;
    FloatType3 a27 = 2.5;

    assertEqual(a24 - a24, 0.0);
    assertEqual(a24 - a25, -3.0);
    assertEqual(a24 - a26, -3.25);
    assertEqual(a24 - a27, -4.5);
    assertEqual(a25 - a25, 0.0);
    assertEqual(a25 - a26, -0.25);
    assertEqual(a25 - a27, -1.5);
    assertEqual(a26 - a26, 0.0);
    assertEqual(a26 - a27, -1.25);
    assertEqual(a27 - a27, 0.0);

    DecimalType1 a28 = 1.25;
    DecimalType3 a29 = 2;
    DecimalType3 a30 = 3;

    assertEqual(a28 - a28, 0d);
    assertEqual(a28 - a29, -0.75d);
    assertEqual(a28 - a30, -1.75d);
    assertEqual(a29 - a29, 0d);
    assertEqual(a29 - a30, -1d);
    assertEqual(a30 - a30, 0d);
}

function testSubtractSingleton() {
    20 a1 = 20;
    int a2 = 2;
    20.5 a3 = 20.5;
    float a4 = 10.5;
    SomeTypes a5 = 10;
    int|int:Signed16 a6 = 15;
    E a7 = 12;

    assertEqual(a1 - a2, 18);
    assertEqual(a3 - a4, 10.0);
    assertEqual(a1 - a5, 10);
    assertEqual(a1 - a6, 5);
    assertEqual(a1 - a7, 8);
}

type Ints 1|2;
type T1 1|2|()|3;
type T2 1|2|3?;
type Decimals 1d|2d;

function testSubNullable() {
    int? a1 = 5;
    int? a2 = 6;
    int? a3 = 10;
    int? a4 = ();
    int a5 = 12;
    float? a6 = 5.5;
    float? a7 = 10.0;
    float? a8 = ();
    float a9 = 5.0;

    int? a10 = a2 - a1;
    int? a11 = a5 - a3;
    int? a12 = a4 - a1;
    float? a13 = a7 - a6;
    float? a14 = a6 - a9;
    float? a15 = a6 - a8;

    Ints a16 = 2;
    int? a17 = 1;
    int? a18 = a16 - a17;

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

    assertEqual(a10, 1);
    assertEqual(a11, 2);
    assertEqual(a12, ());
    assertEqual(a13, 4.5);
    assertEqual(a14, 0.5);
    assertEqual(a15, ());
    assertEqual(a18, 1);
    assertEqual(a19 - a20, 23);

    assertEqual(a21 - a21, 0);
    assertEqual(a21 - a22, 1);
    assertEqual(a21 - a23, ());
    assertEqual(a22 - a22, 0);
    assertEqual(a22 - a23, ());
    assertEqual(a23 - a23, ());
    assertEqual(a21 - a24, 1);
    assertEqual(a26 - a25, 1d);

    assertEqual(a - a, 0);
    assertEqual(a - b, -1);
    assertEqual(a - c, -4);
    assertEqual(a - d, -19);
    assertEqual(a - e, -9);
    assertEqual(a - f, -9);
    assertEqual(a - g, -29);

    assertEqual(b - c, -3);
    assertEqual(b - d, -18);
    assertEqual(b - e, -8);
    assertEqual(b - f, -8);
    assertEqual(b - g, -28);
    assertEqual(b - b, 0);

    assertEqual(c - c, 0);
    assertEqual(c - d, -15);
    assertEqual(c - e, -5);
    assertEqual(c - f, -5);
    assertEqual(c - g, -25);

    assertEqual(d - d, 0);
    assertEqual(d - e, 10);
    assertEqual(d - f, 10);
    assertEqual(d - g, -10);

    assertEqual(e - e, 0);
    assertEqual(e - f, 0);
    assertEqual(e - g, -20);

    assertEqual(f - f, 0);
    assertEqual(f - g, -20);

    assertEqual(g - g, 0);

    IntType3? a27 = 1;
    IntType3? a28 = 255;
    IntType4? a29 = 127;

    assertEqual(a27 - a27, 0);
    assertEqual(a27 - a28, -254);
    assertEqual(a27 - a29, -126);
    assertEqual(a28 - a29, 128);
    assertEqual(a29 - a29, 0);

    FloatType2? a30 = -2;
    FloatType2? a31 = 1;
    FloatType3? a32 = 1.25;
    FloatType3? a33 = 2.5;

    assertEqual(a30 - a30, 0.0);
    assertEqual(a30 - a31, -3.0);
    assertEqual(a30 - a32, -3.25);
    assertEqual(a30 - a33, -4.5);
    assertEqual(a31 - a31, -0.0);
    assertEqual(a31 - a32, -0.25);
    assertEqual(a31 - a33, -1.5);
    assertEqual(a32 - a32, 0.0);
    assertEqual(a32 - a33, -1.25);
    assertEqual(a33 - a33, 0.0);

    DecimalType1? a34 = 1.25;
    DecimalType3? a35 = 2;
    DecimalType3? a36 = 3;

    assertEqual(a34 - a34, 0d);
    assertEqual(a34 - a35, -0.75d);
    assertEqual(a34 - a36, -1.75d);
    assertEqual(a35 - a35, 0d);
    assertEqual(a35 - a36, -1d);
    assertEqual(a36 - a36, 0d);
}

function testContextuallyExpectedTypeOfNumericLiteralInSubtract() {
    float a1 = 10.0 - 5 - 2.0;
    float a2 = 10 - 2;
    decimal a3 = 30 - 15.0;
    decimal a4 = 20.0 - 10.0 - 5;
    float? a5 = 20 - 10.0;
    decimal? a6 = 20 - 5.0;

    assertEqual(a1, 3.0);
    assertEqual(a2, 8.0);
    assertEqual(a3, 15.0d);
    assertEqual(a4, 5.0d);
    assertEqual(a5, 10.0);
    assertEqual(a6, 15.0d);
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
