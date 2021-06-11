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

function testContextuallyExpectedTypeOfNumericLiteralInDivision(){
    float a1 = 10.0;
    float a2 = a1 / 5;
    decimal a3 = 15.0;
    decimal a4 = 9.0;

    assertEqual(a1 / 2, 5.0);
    assertEqual(a2, 2.0);
    assertEqual(a3 / 3, 5.0d);
    assertEqual(a4 / 1.5, 6.0d);
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
