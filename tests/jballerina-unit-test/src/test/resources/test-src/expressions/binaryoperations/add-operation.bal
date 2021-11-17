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

type IntType1 -2|-1|0|1|2;
type IntType2 int:Unsigned8|int:Signed32;
type IntType3 IntType1|IntType2;
type IntType4 IntType1|byte;

const float AA = 1.25;
const float BB = 2.5;

type FloatType1 -2.0f|-1.0f|0.0f|1.0f|2.0f;
type FloatType2 FloatType1;
type FloatType3 AA|BB;

const decimal CC = 1.25;
const decimal DD = 3.0;

type DecimalType1 CC|DD;
type DecimalType2 1d|2d|-1d|2d;
type DecimalType3 DecimalType1|DecimalType2;

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

    IntType3 a21 = 1;
    int|IntType3 a22 = 255;
    IntType4|int a23 = 127;

    test:assertEquals(a21 + a21, 2);
    test:assertEquals(a21 + a22, 256);
    test:assertEquals(a21 + a23, 128);
    test:assertEquals(a22 + a23, 382);
    test:assertEquals(a23 + a23, 254);

    FloatType2 a24 = -2;
    FloatType2 a25 = 1;
    FloatType3|float a26 = 1.25;
    float|FloatType3 a27 = 2.5;

    test:assertEquals(a24 + a24, -4.0);
    test:assertEquals(a24 + a25, -1.0);
    test:assertEquals(a24 + a26, -0.75);
    test:assertEquals(a24 + a27, 0.5);
    test:assertEquals(a25 + a25, 2.0);
    test:assertEquals(a25 + a26, 2.25);
    test:assertEquals(a25 + a27, 3.5);
    test:assertEquals(a26 + a26, 2.5);
    test:assertEquals(a26 + a27, 3.75);
    test:assertEquals(a27 + a27, 5.0);

    DecimalType1 a28 = 1.25;
    decimal|DecimalType3 a29 = 2;
    DecimalType3|decimal a30 = 3;

    test:assertEquals(a28 + a28, 2.5d);
    test:assertEquals(a28 + a29, 3.25d);
    test:assertEquals(a28 + a30, 4.25d);
    test:assertEquals(a29 + a29, 4d);
    test:assertEquals(a29 + a30, 5d);
    test:assertEquals(a30 + a30, 6d);
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
    float? a5 = 10 + 5;
    decimal? a6 = 5 + 3;

    test:assertEquals(a1, 15.0);
    test:assertEquals(a2, 18.0);
    test:assertEquals(a3, 20.0d);
    test:assertEquals(a4, 25.0d);
    test:assertEquals(a5, 15.0);
    test:assertEquals(a6, 8.0d);
}

type Ints 1|2;
type T1 1|2|()|3;
type T2 1|2|3?;
type Decimals 1d|2d;

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
    T2? a22 = 1;
    ()|int a23 = ();
    T2? a24 = 1;

    Decimals? a25 = 1;
    Decimals? a26 = 2;

    int:Unsigned8 a = 1;
    int:Unsigned16 b = 2;
    int:Unsigned32 c = 5;
    int:Signed8 d = 20;
    int:Signed16 e = 10;
    int:Signed32 f = 10;
    byte g = 30;

    test:assertEquals(a10, 33);
    test:assertEquals(a11, 22);
    test:assertEquals(a12, ());
    test:assertEquals(a13, 15.5);
    test:assertEquals(a14, 10.5);
    test:assertEquals(a15, ());
    test:assertEquals(a18, 3);
    test:assertEquals(a19 + a20, 27);

    test:assertEquals(a21 + a21, 4);
    test:assertEquals(a21 + a22, 3);
    test:assertEquals(a21 + a23, ());
    test:assertEquals(a22 + a22, 2);
    test:assertEquals(a22 + a23, ());
    test:assertEquals(a23 + a23, ());
    test:assertEquals(a24 + a21, 3);
    test:assertEquals(a25 + a26, 3d);

    test:assertEquals(a + a, 2);
    test:assertEquals(a + b, 3);
    test:assertEquals(a + c, 6);
    test:assertEquals(a + d, 21);
    test:assertEquals(a + e, 11);
    test:assertEquals(a + f, 11);
    test:assertEquals(a + g, 31);

    test:assertEquals(b + c, 7);
    test:assertEquals(b + d, 22);
    test:assertEquals(b + e, 12);
    test:assertEquals(b + f, 12);
    test:assertEquals(b + g, 32);
    test:assertEquals(b + b, 4);

    test:assertEquals(c + c, 10);
    test:assertEquals(c + d, 25);
    test:assertEquals(c + e, 15);
    test:assertEquals(c + f, 15);
    test:assertEquals(c + g, 35);

    test:assertEquals(d + d, 40);
    test:assertEquals(d + e, 30);
    test:assertEquals(d + f, 30);
    test:assertEquals(d + g, 50);

    test:assertEquals(e + e, 20);
    test:assertEquals(e + f, 20);
    test:assertEquals(e + g, 40);

    test:assertEquals(f + f, 20);
    test:assertEquals(f + g, 40);

    IntType3? a27 = 1;
    IntType3? a28 = 255;
    IntType4? a29 = 127;

    test:assertEquals(a27 + a27, 2);
    test:assertEquals(a27 + a28, 256);
    test:assertEquals(a27 + a29, 128);
    test:assertEquals(a28 + a29, 382);
    test:assertEquals(a29 + a29, 254);

    FloatType2? a30 = -2;
    FloatType2? a31 = 1;
    FloatType3? a32 = 1.25;
    FloatType3? a33 = 2.5;

    test:assertEquals(a30 + a30, -4.0);
    test:assertEquals(a30 + a31, -1.0);
    test:assertEquals(a30 + a32, -0.75);
    test:assertEquals(a30 + a33, 0.5);
    test:assertEquals(a31 + a31, 2.0);
    test:assertEquals(a31 + a32, 2.25);
    test:assertEquals(a31 + a33, 3.5);
    test:assertEquals(a32 + a32, 2.5);
    test:assertEquals(a32 + a33, 3.75);
    test:assertEquals(a33 + a33, 5.0);

    DecimalType1? a34 = 1.25;
    DecimalType3? a35 = 2;
    DecimalType3? a36 = 3;

    test:assertEquals(a34 + a34, 2.5d);
    test:assertEquals(a34 + a35, 3.25d);
    test:assertEquals(a34 + a36, 4.25d);
    test:assertEquals(a35 + a35, 4d);
    test:assertEquals(a35 + a36, 5d);
    test:assertEquals(a36 + a36, 6d);
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
