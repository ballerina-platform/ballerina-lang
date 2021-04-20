function intMultiply(int a, int b) returns (int) {
    return a * b;
}

function floatMultiply(float a, float b) returns (float) {
    return a * b;
}

function intFloatMultiply(int a, float b) returns (float) {
    return a * b;
}

function floatIntMultiply(float a, int b) returns (float) {
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

    assertEqual(a1 * a2, 200);
    assertEqual(a2 * a3, 600);
    assertEqual(a3 * a4, 750);
    assertEqual(a1 * a5, 150);
    assertEqual(a1 * a6, 120);
    assertEqual(a4 * a6, 300);
    assertEqual(a5 * a6, 180);
    assertEqual(a1 * a7, 105.0);
    assertEqual(a5 * a7, 157.5);
    assertEqual(a6 * a7, 126.0);
    assertEqual(a1 * a8, 105.0);
    assertEqual(a2 * a8, 210.0);
    assertEqual(a4 * a8, 262.5);
    assertEqual(a5 * a8, 157.5);
    assertEqual(a6 * a8, 126.0);
    assertEqual(a7 * a8, 110.25);
    assertEqual(a1 * a9, 305.0);
    assertEqual(a2 * a9, 610.0);
    assertEqual(a4 * a9, 762.5);
    assertEqual(a5 * a9, 457.5);
    assertEqual(a6 * a9, 366.0);
    assertEqual(a7 * a9, 320.25);
    assertEqual(a8 * a9, 320.25);
    assertEqual(a1 * a10, 105d);
    assertEqual(a2 * a10, 210d);
    assertEqual(a4 * a10, 262.5d);
    assertEqual(a5 * a10, 157.5d);
    assertEqual(a6 * a10, 126d);
    assertEqual(a7 * a10, 110.25d);
    assertEqual(a8 * a10, 110.25d);
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
