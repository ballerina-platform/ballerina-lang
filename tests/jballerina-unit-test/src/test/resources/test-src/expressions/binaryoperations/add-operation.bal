function intAdd(int a, int b) returns (int) {
    return a + b;
}

function floatAdd(float a, float b) returns (float) {
    return a + b;
}

function stringAdd(string a, string b) returns (string) {
    return a + b;
}

function stringAndIntAdd(string a, int b) returns (string) {
    return a + b.toString();
}

function intFloatAdd(int a, float b) returns (float) {
    return a + b;
}

function floatIntAdd(float a, int b) returns (float) {
    return a + b;
}

function xmlXmlAdd() returns (xml) {
    xml a = xml `abc`;
    xml b = xml `def`;
    return a + b;
}

function xmlStringAdd() returns (xml) {
    xml a = xml `abc`;
    string b = "def";
    return a + b;
}

function stringXmlAdd() returns (xml) {
    string a = "def";
    xml b = xml `abc`;
    return a + b;
}

public const A = 10;
public const B = 20;
public const C = 30;
public const D = 40;

type SomeTypes A|B|C|D;

type E 12|13|14;

const float F = 20.25;
const float G = 10.5;

type H F|G;

type I 10.5|30.4;

const decimal J = 4.565;
const decimal K = 10.5;

type L J|K;

const M = "M";
const N = "N";

type O M|N;

type P "Cat"|"Dog";

function testAdditionWithTypes() {
    SomeTypes a1 = 10;
    int a2 = 20;
    SomeTypes a3 = 30;
    byte a4 = 25;
    int|int:Signed16 a5 = 15;
    E a6 = 12;
    float a7 = 10.5;
    H a8 = 10.5;
    I a9 = 30.4;
    L a10 = 10.5;

    assertEqual(a1 + a2, 30);
    assertEqual(a2 + a3, 50);
    assertEqual(a3 + a4, 55);
    assertEqual(a1 + a5, 25);
    assertEqual(a1 + a6, 22);
    assertEqual(a4 + a6, 37);
    assertEqual(a5 + a6, 27);
    assertEqual(a1 + a7, 20.5);
    assertEqual(a5 + a7, 25.5);
    assertEqual(a6 + a7, 22.5);
    assertEqual(a1 + a8, 20.5);
    assertEqual(a2 + a8, 30.5);
    assertEqual(a4 + a8, 35.5);
    assertEqual(a5 + a8, 25.5);
    assertEqual(a6 + a8, 22.5);
    assertEqual(a7 + a8, 21.0);
    assertEqual(a1 + a9, 40.4);
    assertEqual(a2 + a9, 50.4);
    assertEqual(a4 + a9, 55.4);
    assertEqual(a5 + a9, 45.4);
    assertEqual(a6 + a9, 42.4);
    assertEqual(a7 + a9, 40.9);
    assertEqual(a8 + a9, 40.9);
    assertEqual(a1 + a10, 20.5d);
    assertEqual(a2 + a10, 30.5d);
    assertEqual(a4 + a10, 35.5d);
    assertEqual(a5 + a10, 25.5d);
    assertEqual(a6 + a10, 22.5d);
    assertEqual(a7 + a10, 21d);
    assertEqual(a8 + a10, 21d);

    string a11 = "abc";
    O a12 = "M";
    string|string:Char a13 = "EFG";
    O a14 = "N";
    P a15 = "Cat";
    xml a16 = xml `abc`;

    assertEqual(a11 + a12, "abcM");
    assertEqual(a12 + a13, "MEFG");
    assertEqual(a12 + a14, "MN");
    assertEqual(a12 + a1.toString(), "M10");
    assertEqual(a12 + a15, "MCat");
    assertEqual(a12 + a16, xml `Mabc`);
    assertEqual(a13 + a16, xml `EFGabc`);
    assertEqual(a14 + a16, xml `Nabc`);
    assertEqual(a15 + a16, xml `Catabc`);
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
