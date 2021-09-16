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
    int? a21 = a19 / a20;

    assertEqual(a10, 1);
    assertEqual(a11, 5);
    assertEqual(a12, ());
    assertEqual(a13, 3.0);
    assertEqual(a14, 6.0);
    assertEqual(a15, ());
    assertEqual(a16, 2);
    assertEqual(a21, 15);
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
