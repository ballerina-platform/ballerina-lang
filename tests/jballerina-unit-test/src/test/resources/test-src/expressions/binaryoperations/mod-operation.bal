function intMod(int a, int b) returns (int) {
    return a % b;
}

function floatMod(float a, float b) returns (float) {
    return a % b;
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

type I 10.0|30.0;

const decimal J = 4.565;
const decimal K = 10.0;

type L J|K;

function testModWithTypes() {
    SomeTypes a1 = 10;
    int a2 = 20;
    SomeTypes a3 = 30;
    byte a4 = 25;
    int|int:Signed16 a5 = 15;
    E a6 = 12;
    float a7 = 10.0;
    H a8 = 10.0;
    I a9 = 30.0;
    L a10 = 10.0;
    decimal a11 = 20.0;

    assertEqual(a1 % a2, 10);
    assertEqual(a2 % a3, 20);
    assertEqual(a3 % a4, 5);
    assertEqual(a1 % a5, 10);
    assertEqual(a1 % a6, 10);
    assertEqual(a4 % a6, 1);
    assertEqual(a5 % a6, 3);
    assertEqual(a7 % a8, 0.0);
    assertEqual(a7 % a9, 10.0);
    assertEqual(a8 % a9, 10.0);
    assertEqual(a10 % a11, 10d);
}

function testModSingleton() {
    20 a1 = 20;
    int a2 = 2;
    25.0 a3 = 25.0;
    float a4 = 10;
    SomeTypes a5 = 30;
    int|int:Signed16 a6 = 5;
    E a7 = 12;

    assertEqual(a1 % a2, 0);
    assertEqual(a3 % a4, 5.0);
    assertEqual(a1 % a5, 20);
    assertEqual(a1 % a6, 0);
    assertEqual(a1 % a7, 8);
}

function testModNullable() {
    int? a1 = 10;
    int? a2 = 3;
    int? a3 = 5;
    int? a4 = ();
    int a5 = 2;
    float? a6 = 10.0;
    float? a7 = 5.0;
    float? a8 = ();
    float a9 = 4.0;

    int? a10 = a1 % a5;
    int? a11 = a3 % a2;
    int? a12 = a3 % a4;
    float? a13 = a6 % a7;
    float? a14 = a7 % a9;
    float? a15 = a8 % a9;

    assertEqual(a10, 0);
    assertEqual(a11, 2);
    assertEqual(a12, ());
    assertEqual(a13, 0.0);
    assertEqual(a14, 1.0);
    assertEqual(a15, ());
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
