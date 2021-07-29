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

function testContextuallyExpectedTypeOfNumericLiteralInSubtract() {
    float a1 = 10.0 - 5 - 2.0;
    float a2 = 10 - 2;
    decimal a3 = 30 - 15.0;
    decimal a4 = 20.0 - 10.0 - 5;

    assertEqual(a1, 3.0);
    assertEqual(a2, 8.0);
    assertEqual(a3, 15.0d);
    assertEqual(a4, 5.0d);
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
