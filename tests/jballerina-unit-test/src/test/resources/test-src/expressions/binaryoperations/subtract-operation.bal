function intSubtract(int a, int b) returns (int) {
    return a - b;
}

function floatSubtract(float a, float b) returns (float) {
    return a - b;
}


function intFloatSubtract(int a, float b) returns (float) {
    return a - b;
}

function floatIntSubtract(float a, int b) returns (float) {
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

    assertEqual(a2 - a1, 10);
    assertEqual(a3 - a2, 10);
    assertEqual(a3 - a4, 5);
    assertEqual(a1 - a5, -5);
    assertEqual(a1 - a6, -2);
    assertEqual(a4 - a6, 13);
    assertEqual(a5 - a6, 3);
    assertEqual(a7 - a1, 0.5);
    assertEqual(a5 - a7, 4.5);
    assertEqual(a6 - a7, 1.5);
    assertEqual(a1 - a8, -0.5);
    assertEqual(a2 - a8, 9.5);
    assertEqual(a4 - a8, 14.5);
    assertEqual(a5 - a8, 4.5);
    assertEqual(a6 - a8, 1.5);
    assertEqual(a7 - a8, 0.0);
    assertEqual(a1 - a9, -20.5);
    assertEqual(a2 - a9, -10.5);
    assertEqual(a4 - a9, -5.5);
    assertEqual(a5 - a9, -15.5);
    assertEqual(a6 - a9, -18.5);
    assertEqual(a7 - a9, -20.0);
    assertEqual(a8 - a9, -20.0);
    assertEqual(a1 - a10, -0.5d);
    assertEqual(a2 - a10, 9.5d);
    assertEqual(a4 - a10, 14.5d);
    assertEqual(a5 - a10, 4.5d);
    assertEqual(a6 - a10, 1.5d);
    assertEqual(a7 - a10, 0d);
    assertEqual(a8 - a10, 0d);
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
