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
    int? a21 = a19 * a20;

    assertEqual(a10, 100);
    assertEqual(a11, 5);
    assertEqual(a12, ());
    assertEqual(a13, 300.0);
    assertEqual(a14, 150.0);
    assertEqual(a15, ());
    assertEqual(a18, 2);
    assertEqual(a21, 50);
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
