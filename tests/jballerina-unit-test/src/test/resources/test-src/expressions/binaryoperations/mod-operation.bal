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

type Ints 1|2;
type T1 1|2|()|3;
type T2 1|2|3?;

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

    Ints a16 = 2;
    int? a17 = 1;
    int? a18 = a16 % a17;

    int a19 = 25;
    Ints? a20 = 2;

    T1 a21 = 2;
    T2 a22 = 1;
    ()|int a23 = ();

    int:Unsigned8 a = 1;
    int:Unsigned16 b = 2;
    int:Unsigned32 c = 5;
    int:Signed8 d = 20;
    int:Signed16 e = 10;
    int:Signed32 f = 10;
    byte g = 30;

    assertEqual(a10, 0);
    assertEqual(a11, 2);
    assertEqual(a12, ());
    assertEqual(a13, 0.0);
    assertEqual(a14, 1.0);
    assertEqual(a15, ());
    assertEqual(a18, 0);
    assertEqual(a19 % a20, 1);

    assertEqual(a21 % a21, 0);
    assertEqual(a21 % a22, 0);
    assertEqual(a21 % a23, ());
    assertEqual(a22 % a22, 0);
    assertEqual(a22 % a23, ());
    assertEqual(a23 % a23, ());

    assertEqual(a % a, 0);
    assertEqual(a % b, 1);
    assertEqual(a % c, 1);
    assertEqual(a % d, 1);
    assertEqual(a % e, 1);
    assertEqual(a % f, 1);
    assertEqual(a % g, 1);

    assertEqual(b % c, 2);
    assertEqual(b % d, 2);
    assertEqual(b % e, 2);
    assertEqual(b % f, 2);
    assertEqual(b % g, 2);
    assertEqual(b % b, 0);

    assertEqual(c % c, 0);
    assertEqual(c % d, 5);
    assertEqual(c % e, 5);
    assertEqual(c % f, 5);
    assertEqual(c % g, 5);

    assertEqual(d % d, 0);
    assertEqual(d % e, 0);
    assertEqual(d % f, 0);
    assertEqual(d % g, 20);

    assertEqual(e % e, 0);
    assertEqual(e % f, 0);
    assertEqual(e % g, 10);

    assertEqual(f % f, 0);
    assertEqual(f % g, 10);

    assertEqual(g % g, 0);
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
