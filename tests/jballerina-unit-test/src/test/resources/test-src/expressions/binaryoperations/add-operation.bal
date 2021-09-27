import ballerina/test;

function intAdd(int a, int b) returns (int) {
    return a + b;
}

function overflowByAddition() {
    int num1 = 9223372036854775807;
    int num2 = 1;
    int ans = num1 + num2;
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
    decimal a11 = 20.5;
    decimal a12 = 30.5;
    byte a13 = 15;

    test:assertEquals(a1 + a2, 30);
    test:assertEquals(a2 + a1, 30);
    test:assertEquals(a2 + a3, 50);
    test:assertEquals(a3 + a4, 55);
    test:assertEquals(a1 + a5, 25);
    test:assertEquals(a1 + a6, 22);
    test:assertEquals(a4 + a5, 40);
    test:assertEquals(a4 + a6, 37);
    test:assertEquals(a5 + a6, 27);
    test:assertEquals(a7 + a8, 21.0);
    test:assertEquals(a7 + a9, 40.9);
    test:assertEquals(a8 + a9, 40.9);
    test:assertEquals(a10 + a11, 31d);
    test:assertEquals(a11 + a12, 51d);
    test:assertEquals(a4 + a13, 40);

    string a14 = "abc";
    O a15 = "M";
    string|string:Char a16 = "EFG";
    O a17 = "N";
    P a18 = "Cat";
    xml a19 = xml `abc`;
    xml:Text|xml a20 = xml `abdef`;

    test:assertEquals(a14 + a15, "abcM");
    test:assertEquals(a15 + a16, "MEFG");
    test:assertEquals(a15 + a17, "MN");
    test:assertEquals(a15 + a1.toString(), "M10");
    test:assertEquals(a15 + a18, "MCat");
    test:assertEquals(a15 + a19, xml `Mabc`);
    test:assertEquals(a19 + a15, xml `abcM`);
    test:assertEquals(a16 + a19, xml `EFGabc`);
    test:assertEquals(a17 + a19, xml `Nabc`);
    test:assertEquals(a18 + a19, xml `Catabc`);
    test:assertEquals(a19 + a20, xml `abcabdef`);
    test:assertEquals(a20 + a19, xml `abdefabc`);
}

function testAddSingleton() {
    1 a1 = 1;
    int a2 = 2;
    20.5 a3 = 20.5;
    float a4 = 10.5;
    SomeTypes a5 = 10;
    int|int:Signed16 a6 = 15;
    E a7 = 12;

    test:assertEquals(a1 + a2, 3);
    test:assertEquals(a3 + a4, 31.0);
    test:assertEquals(a1 + a5, 11);
    test:assertEquals(a1 + a6, 16);
    test:assertEquals(a1 + a7, 13);
}

function testContextuallyExpectedTypeOfNumericLiteralInAdd() {
    float a1 = 10.0 + 5;
    float a2 = 5 + 3 + 10.0;
    decimal a3 = 5 + 15.0;
    decimal a4 = 5.0 + 10.0 + 10;

    test:assertEquals(a1, 15.0);
    test:assertEquals(a2, 18.0);
    test:assertEquals(a3, 20.0d);
    test:assertEquals(a4, 25.0d);
}

type Strings "x"|"yz";

function testStringCharAddition() {
    string s = "abc";
    string:Char c = "d";
    string|(string|string:Char) a = "efg";
    Strings b = "x";
    Strings d = "yz";

    test:assertEquals(s + c, "abcd");
    test:assertEquals(c + s, "dabc");
    test:assertEquals(s + a, "abcefg");
    test:assertEquals(a + s, "efgabc");
    test:assertEquals(c + a, "defg");
    test:assertEquals(a + c, "efgd");
    test:assertEquals(s + b, "abcx");
    test:assertEquals(b + s, "xabc");
    test:assertEquals(c + d, "dyz");
    test:assertEquals(d + c, "yzd");
}

function testStringXmlSubtypesAddition() {
    string s = "abc";
    string:Char c = "d";
    xml x1 = xml `efg`;
    xml:Text x2 = xml `text`;

    test:assertEquals(s + x2, xml `abctext`);
    test:assertEquals(x2 + s, xml `textabc`);
    test:assertEquals(c + x1, xml `defg`);
    test:assertEquals(x1 + c, xml `efgd`);
    test:assertEquals(c + x2, xml `dtext`);
    test:assertEquals(x2 + c, xml `textd`);
}
