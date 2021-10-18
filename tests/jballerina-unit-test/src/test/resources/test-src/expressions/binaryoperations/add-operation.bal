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
    assertEqual(a1, 15.0);
    assertEqual(a2, 18.0);
    assertEqual(a3, 20.0d);
    assertEqual(a4, 25.0d);
    assertEqual(a5, 15.0);
    assertEqual(a6, 8.0d);
}

type Ints 1|2;
type T1 1|2|()|3;
type T2 1|2|3?;

function testAddNullable() {
    int? a1 = 5;
    int? a2 = 6;
    int? a3 = 10;
    int? a4 = ();
    int a5 = 12;
    float? a6 = 5.5;
    float? a7 = 10.0;
    float? a8 = ();
    float a9 = 5.0;

    int? a10 = a1 + a2 + a3 + a5;
    int? a11 = a5 + a3;
    int? a12 = a4 + a1;
    float? a13 = a6 + a7;
    float? a14 = a6 + a9;
    float? a15 = a6 + a8;

    Ints a16 = 2;
    int? a17 = 1;
    int? a18 = a16 + a17;

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

    assertEqual(a10, 33);
    assertEqual(a11, 22);
    assertEqual(a12, ());
    assertEqual(a13, 15.5);
    assertEqual(a14, 10.5);
    assertEqual(a15, ());
    assertEqual(a18, 3);
    assertEqual(a19 + a20, 27);

    assertEqual(a21 + a21, 4);
    assertEqual(a21 + a22, 3);
    assertEqual(a21 + a23, ());
    assertEqual(a22 + a22, 2);
    assertEqual(a22 + a23, ());
    assertEqual(a23 + a23, ());

    assertEqual(a + a, 2);
    assertEqual(a + b, 3);
    assertEqual(a + c, 6);
    assertEqual(a + d, 21);
    assertEqual(a + e, 11);
    assertEqual(a + f, 11);
    assertEqual(a + g, 31);

    assertEqual(b + c, 7);
    assertEqual(b + d, 22);
    assertEqual(b + e, 12);
    assertEqual(b + f, 12);
    assertEqual(b + g, 32);
    assertEqual(b + b, 4);

    assertEqual(c + c, 10);
    assertEqual(c + d, 25);
    assertEqual(c + e, 15);
    assertEqual(c + f, 15);
    assertEqual(c + g, 35);

    assertEqual(d + d, 40);
    assertEqual(d + e, 30);
    assertEqual(d + f, 30);
    assertEqual(d + g, 50);

    assertEqual(e + e, 20);
    assertEqual(e + f, 20);
    assertEqual(e + g, 40);

    assertEqual(f + f, 20);
    assertEqual(f + g, 40);

    assertEqual(g + g, 60);
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
