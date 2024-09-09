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

    assertEquality(a1 + a2, 30);
    assertEquality(a2 + a1, 30);
    assertEquality(a2 + a3, 50);
    assertEquality(a3 + a4, 55);
    assertEquality(a1 + a5, 25);
    assertEquality(a1 + a6, 22);
    assertEquality(a4 + a5, 40);
    assertEquality(a4 + a6, 37);
    assertEquality(a5 + a6, 27);
    assertEquality(a7 + a8, 21.0);
    assertEquality(a7 + a9, 40.9);
    assertEquality(a8 + a9, 40.9);
    assertEquality(a10 + a11, 31d);
    assertEquality(a11 + a12, 51d);
    assertEquality(a4 + a13, 40);

    string a14 = "abc";
    O a15 = "M";
    string|string:Char a16 = "EFG";
    O a17 = "N";
    P a18 = "Cat";
    xml a19 = xml `abc`;
    xml:Text|xml a20 = xml `abdef`;

    assertEquality(a14 + a15, "abcM");
    assertEquality(a15 + a16, "MEFG");
    assertEquality(a15 + a17, "MN");
    assertEquality(a15 + a1.toString(), "M10");
    assertEquality(a15 + a18, "MCat");
    assertEquality(a15 + a19, xml `Mabc`);
    assertEquality(a19 + a15, xml `abcM`);
    assertEquality(a16 + a19, xml `EFGabc`);
    assertEquality(a17 + a19, xml `Nabc`);
    assertEquality(a18 + a19, xml `Catabc`);
    assertEquality(a19 + a20, xml `abcabdef`);
    assertEquality(a20 + a19, xml `abdefabc`);

    IntType3 a21 = 1;
    int|IntType3 a22 = 255;
    IntType4|int a23 = 127;

    assertEquality(a21 + a21, 2);
    assertEquality(a21 + a22, 256);
    assertEquality(a21 + a23, 128);
    assertEquality(a22 + a23, 382);
    assertEquality(a23 + a23, 254);

    FloatType2 a24 = -2;
    FloatType2 a25 = 1;
    FloatType3|float a26 = 1.25;
    float|FloatType3 a27 = 2.5;

    assertEquality(a24 + a24, -4.0);
    assertEquality(a24 + a25, -1.0);
    assertEquality(a24 + a26, -0.75);
    assertEquality(a24 + a27, 0.5);
    assertEquality(a25 + a25, 2.0);
    assertEquality(a25 + a26, 2.25);
    assertEquality(a25 + a27, 3.5);
    assertEquality(a26 + a26, 2.5);
    assertEquality(a26 + a27, 3.75);
    assertEquality(a27 + a27, 5.0);

    DecimalType1 a28 = 1.25;
    decimal|DecimalType3 a29 = 2;
    DecimalType3|decimal a30 = 3;

    assertEquality(a28 + a28, 2.5d);
    assertEquality(a28 + a29, 3.25d);
    assertEquality(a28 + a30, 4.25d);
    assertEquality(a29 + a29, 4d);
    assertEquality(a29 + a30, 5d);
    assertEquality(a30 + a30, 6d);
}

function testAddSingleton() {
    1 a1 = 1;
    int a2 = 2;
    20.5 a3 = 20.5;
    float a4 = 10.5;
    SomeTypes a5 = 10;
    int|int:Signed16 a6 = 15;
    E a7 = 12;

    assertEquality(a1 + a2, 3);
    assertEquality(a3 + a4, 31.0);
    assertEquality(a1 + a5, 11);
    assertEquality(a1 + a6, 16);
    assertEquality(a1 + a7, 13);
}

function testContextuallyExpectedTypeOfNumericLiteralInAdd() {
    float a1 = 10.0 + 5;
    float a2 = 5 + 3 + 10.0;
    decimal a3 = 5 + 15.0;
    decimal a4 = 5.0 + 10.0 + 10;
    float? a5 = 10 + 5;
    decimal? a6 = 5 + 3;

    assertEquality(a1, 15.0);
    assertEquality(a2, 18.0);
    assertEquality(a3, 20.0d);
    assertEquality(a4, 25.0d);
    assertEquality(a5, 15.0);
    assertEquality(a6, 8.0d);
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

    assertEquality(a10, 33);
    assertEquality(a11, 22);
    assertEquality(a12, ());
    assertEquality(a13, 15.5);
    assertEquality(a14, 10.5);
    assertEquality(a15, ());
    assertEquality(a18, 3);
    assertEquality(a19 + a20, 27);

    assertEquality(a21 + a21, 4);
    assertEquality(a21 + a22, 3);
    assertEquality(a21 + a23, ());
    assertEquality(a22 + a22, 2);
    assertEquality(a22 + a23, ());
    assertEquality(a23 + a23, ());
    assertEquality(a24 + a21, 3);
    assertEquality(a25 + a26, 3d);

    assertEquality(a + a, 2);
    assertEquality(a + b, 3);
    assertEquality(a + c, 6);
    assertEquality(a + d, 21);
    assertEquality(a + e, 11);
    assertEquality(a + f, 11);
    assertEquality(a + g, 31);

    assertEquality(b + c, 7);
    assertEquality(b + d, 22);
    assertEquality(b + e, 12);
    assertEquality(b + f, 12);
    assertEquality(b + g, 32);
    assertEquality(b + b, 4);

    assertEquality(c + c, 10);
    assertEquality(c + d, 25);
    assertEquality(c + e, 15);
    assertEquality(c + f, 15);
    assertEquality(c + g, 35);

    assertEquality(d + d, 40);
    assertEquality(d + e, 30);
    assertEquality(d + f, 30);
    assertEquality(d + g, 50);

    assertEquality(e + e, 20);
    assertEquality(e + f, 20);
    assertEquality(e + g, 40);

    assertEquality(f + f, 20);
    assertEquality(f + g, 40);

    IntType3? a27 = 1;
    IntType3? a28 = 255;
    IntType4? a29 = 127;

    assertEquality(a27 + a27, 2);
    assertEquality(a27 + a28, 256);
    assertEquality(a27 + a29, 128);
    assertEquality(a28 + a29, 382);
    assertEquality(a29 + a29, 254);

    FloatType2? a30 = -2;
    FloatType2? a31 = 1;
    FloatType3? a32 = 1.25;
    FloatType3? a33 = 2.5;

    assertEquality(a30 + a30, -4.0);
    assertEquality(a30 + a31, -1.0);
    assertEquality(a30 + a32, -0.75);
    assertEquality(a30 + a33, 0.5);
    assertEquality(a31 + a31, 2.0);
    assertEquality(a31 + a32, 2.25);
    assertEquality(a31 + a33, 3.5);
    assertEquality(a32 + a32, 2.5);
    assertEquality(a32 + a33, 3.75);
    assertEquality(a33 + a33, 5.0);

    DecimalType1? a34 = 1.25;
    DecimalType3? a35 = 2;
    DecimalType3? a36 = 3;

    assertEquality(a34 + a34, 2.5d);
    assertEquality(a34 + a35, 3.25d);
    assertEquality(a34 + a36, 4.25d);
    assertEquality(a35 + a35, 4d);
    assertEquality(a35 + a36, 5d);
    assertEquality(a36 + a36, 6d);
}

type Strings "x"|"yz";

function testStringCharAddition() {
    string s = "abc";
    string:Char c = "d";
    string|(string|string:Char) a = "efg";
    Strings b = "x";
    Strings d = "yz";

    assertEquality(s + c, "abcd");
    assertEquality(c + s, "dabc");
    assertEquality(s + a, "abcefg");
    assertEquality(a + s, "efgabc");
    assertEquality(c + a, "defg");
    assertEquality(a + c, "efgd");
    assertEquality(s + b, "abcx");
    assertEquality(b + s, "xabc");
    assertEquality(c + d, "dyz");
    assertEquality(d + c, "yzd");
}

function testStringXmlSubtypesAddition() {
    string s = "abc";
    string:Char c = "d";
    xml x1 = xml `efg`;
    xml:Text x2 = xml `text`;
    string:Char|Strings b = "d";
    Strings|Baz e = "bar";
    Bar d = "B";
    xml l = x2 + b;
    xml m = x2 + e;
    xml n = x2 + d;
    xml o = x1 + b;
    xml p = x1 + e;
    xml q = x1 + d;

    assertEquality(s + x2, xml `abctext`);
    assertEquality(x2 + s, xml `textabc`);
    assertEquality(c + x1, xml `defg`);
    assertEquality(x1 + c, xml `efgd`);
    assertEquality(c + x2, xml `dtext`);
    assertEquality(x2 + c, xml `textd`);
    assertEquality(l, xml `textd`);
    assertEquality(m, xml `textbar`);
    assertEquality(n, xml `textB`);
    assertEquality(o, xml `efgd`);
    assertEquality(p, xml `efgbar`);
    assertEquality(q, xml `efgB`);
}

type Baz "B"|"bar";
type Bar Baz|string;

function testStringSubtypesAddition() {
    string a = "abc";
    string:Char|Strings b = "d";
    Strings|Baz c = "bar";
    Bar d = "B";
    string:Char e = "e";
    string f = a + c;
    string g = a + b;
    string h = d + e;
    string i = a + b + a;
    string j = c + b;
    string k = a + e;
    string l = b + e;
    string m = c + e;
    string n = b + e + b;

    assertEquality(f, "abcbar");
    assertEquality(g, "abcd");
    assertEquality(i, "abcdabc");
    assertEquality(j, "bard");
    assertEquality(k, "abce");
    assertEquality(l, "de");
    assertEquality(m, "bare");
    assertEquality(h, "Be");
    assertEquality(n, "ded");
}

type Foo xml<'xml:Text>|xml<'xml:Comment> ;
type Foo2 xml<'xml:Element|'xml:ProcessingInstruction>;
type Foo3 Foo|Foo2;

function testXmlSubtypesAddition() {
    xml a = xml `<foo>foo</foo><?foo?>text1<!--comment-->`;
    xml<'xml:Element>|Foo b = xml `<foo></foo>`;
    Foo2 c = xml `<?foo?><?faa?>`;
    Foo3 d = xml `text1 text2`;
    xml<'xml:Comment> e = xml `<!--comment1--><!--comment2-->`;
    xml<'xml:Text|'xml:Comment> f = xml `<!--comment-->`;
    xml<'xml:Text>|xml<'xml:Comment> g = xml `<!--comment-->`;
    xml<'xml:Element|'xml:ProcessingInstruction> h = xml `<root> text1<foo>100</foo><foo>200</foo></root><?foo?>`;
    xml<'xml:Element>|'xml:Text i = xml `text1 text2`;
    xml j = a + b;
    xml k = a + c;
    xml l = a + d;
    xml m = c + g;
    xml n = c + h;
    xml o = d + f;
    xml p = g + h;
    
    xml result1 = xml `<foo>foo</foo><?foo ?>text1<!--comment--><foo></foo>`;
    xml result2 = xml `<foo>foo</foo><?foo ?>text1<!--comment--><?foo ?><?faa ?>`;
    xml result3 = xml `<foo>foo</foo><?foo ?>text1<!--comment-->text1 text2`;
    xml result4 = xml `<?foo ?><?faa ?><!--comment-->`;
    xml result5 = xml `<?foo ?><?faa ?><root> text1<foo>100</foo><foo>200</foo></root><?foo ?>`;
    xml result6 = xml `text1 text2<!--comment-->`;
    xml result7 = xml `<!--comment--><root> text1<foo>100</foo><foo>200</foo></root><?foo ?>`;
    assertEquality(j, result1);
    assertEquality(k, result2);
    assertEquality(l, result3);
    assertEquality(m, result4);
    assertEquality(n, result5);
    assertEquality(o, result6);
    assertEquality(p, result7);
}

int intVal = 10;

function testNoShortCircuitingInAdditionWithNullable() {
    int? result = foo() + bar();
    assertEquality(result, ());
    assertEquality(intVal, 18);

    result = foo() + 12;
    assertEquality(result, ());
    assertEquality(intVal, 20);

    result = 12 + bar();
    assertEquality(result, ());
    assertEquality(intVal, 26);

    int? x = 12;
    result = foo() + x;
    assertEquality(result, ());
    assertEquality(intVal, 28);

    result = x + bar();
    assertEquality(result, ());
    assertEquality(intVal, 34);

    result = x + bam();
    assertEquality(result, 17);
    assertEquality(intVal, 44);

    result = bam() + x;
    assertEquality(result, 17);
    assertEquality(intVal, 54);

    result = foo() + bam();
    assertEquality(result, ());
    assertEquality(intVal, 66);

    result = bam() + bar();
    assertEquality(result, ());
    assertEquality(intVal, 82);
}

function testNoShortCircuitingInAdditionWithNonNullable() {
    intVal = 10;
    int x = 10;

    int result = x + bam();
    assertEquality(result, 15);
    assertEquality(intVal, 20);

    result = bam() + 12;
    assertEquality(result, 17);
    assertEquality(intVal, 30);
}

function foo() returns int? {
    intVal += 2;
    return ();
}

function bar() returns int? {
    intVal += 6;
    return ();
}

function bam() returns int {
    intVal += 10;
    return 5;
}

function testNullableIntAddition() {
    int[] result = [1, 2];
    int? val = baz() + result[0];
    assertEquality(val, ());
}

function baz() returns int? {
    return ();
}

type S string;

type P2 "s6"|"s7";

function testXmlStringAddition() {
    string s1 = "s1";
    string:Char s2 = "s";
    "s3" s3 = "s3";
    "s4"|"s5" s4 = "s4";
    S s5 = "s5";
    P2 s6 = "s6";

    xml<xml:Element> x1 = xml `<a>a</a>`;
    xml<xml:Element|xml:Text> r1 = x1 + s1;
    assertEquality(x1 + s1, xml `<a>a</a>s1`);
    r1 = x1 + s2;
    assertEquality(r1, xml `<a>a</a>s`);
    r1 = x1 + s3;
    assertEquality(r1, xml `<a>a</a>s3`);
    r1 = x1 + s4;
    assertEquality(r1, xml `<a>a</a>s4`);
    r1 = x1 + s5;
    assertEquality(r1, xml `<a>a</a>s5`);
    r1 = x1 + s6;
    assertEquality(r1, xml `<a>a</a>s6`);

    xml<xml:Comment> x2 = xml `<!--comment-->`;
    xml<xml:Comment|xml:Text> r2 = x2 + s1;
    assertEquality(r2, xml `<!--comment-->s1`);
    r2 = x2 + s2;
    assertEquality(r2, xml `<!--comment-->s`);
    r2 = x2 + s3;
    assertEquality(r2, xml `<!--comment-->s3`);
    r2 = x2 + s4;
    assertEquality(r2, xml `<!--comment-->s4`);
    r2 = x2 + s5;
    assertEquality(r2, xml `<!--comment-->s5`);
    r2 = x2 + s6;
    assertEquality(r2, xml `<!--comment-->s6`);

    xml<xml:ProcessingInstruction> x3 = xml `<?data?>`;
    xml<xml:ProcessingInstruction|xml:Text> r3 = x3 + s1;
    assertEquality(r3, xml `<?data?>s1`);
    r3 = x3 + s2;
    assertEquality(r3, xml `<?data?>s`);
    r3 = x3 + s3;
    assertEquality(r3, xml `<?data?>s3`);
    r3 = x3 + s4;
    assertEquality(r3, xml `<?data?>s4`);
    r3 = x3 + s5;
    assertEquality(r3, xml `<?data?>s5`);
    r3 = x3 + s6;
    assertEquality(r3, xml `<?data?>s6`);

    xml:Text x4 = xml `Xml text`;
    xml:Text r4 = x4 + s1;
    assertEquality(r4, xml `Xml texts1`);
    r4 = x4 + s2;
    assertEquality(r4, xml `Xml texts`);
    r4 = x4 + s3;
    assertEquality(r4, xml `Xml texts3`);
    r4 = x4 + s4;
    assertEquality(r4, xml `Xml texts4`);
    r4 = x4 + s5;
    assertEquality(r4, xml `Xml texts5`);
    r4 = x4 + s6;
    assertEquality(r4, xml `Xml texts6`);

    xml<xml:Element|xml:Comment> x5 = xml `<a>a</a><b><!--comment--></b><c>c</c>`;
    xml<xml:Element|xml:Comment|xml:Text> r5 = x5 + s1;
    assertEquality(r5, xml `<a>a</a><b><!--comment--></b><c>c</c>s1`);
    r5 = x5 + s2;
    assertEquality(r5, xml `<a>a</a><b><!--comment--></b><c>c</c>s`);
    r5 = x5 + s3;
    assertEquality(r5, xml `<a>a</a><b><!--comment--></b><c>c</c>s3`);
    r5 = x5 + s4;
    assertEquality(r5, xml `<a>a</a><b><!--comment--></b><c>c</c>s4`);
    r5 = x5 + s5;
    assertEquality(r5, xml `<a>a</a><b><!--comment--></b><c>c</c>s5`);
    r5 = x5 + s6;
    assertEquality(r5, xml `<a>a</a><b><!--comment--></b><c>c</c>s6`);

    xml<xml:Element|xml:ProcessingInstruction> x6 = xml `<a>a</a><b>b</b><?pi?>`;
    xml<xml:Element|xml:ProcessingInstruction|xml:Text> r6 = x6 + s1;
    assertEquality(r6, xml `<a>a</a><b>b</b><?pi?>s1`);
    r6 = x6 + s2;
    assertEquality(r6, xml `<a>a</a><b>b</b><?pi?>s`);
    r6 = x6 + s3;
    assertEquality(r6, xml `<a>a</a><b>b</b><?pi?>s3`);
    r6 = x6 + s4;
    assertEquality(r6, xml `<a>a</a><b>b</b><?pi?>s4`);
    r6 = x6 + s5;
    assertEquality(r6, xml `<a>a</a><b>b</b><?pi?>s5`);
    r6 = x6 + s6;
    assertEquality(r6, xml `<a>a</a><b>b</b><?pi?>s6`);

    xml<xml:Element|xml:Text> x7 = xml `<a>a</a><b>b</b>Xml text`;
    xml<xml:Element|xml:Text> r7 = x7 + s1;
    assertEquality(r7, xml `<a>a</a><b>b</b>Xml texts1`);
    r7 = x7 + s2;
    assertEquality(r7, xml `<a>a</a><b>b</b>Xml texts`);
    r7 = x7 + s3;
    assertEquality(r7, xml `<a>a</a><b>b</b>Xml texts3`);
    r7 = x7 + s4;
    assertEquality(r7, xml `<a>a</a><b>b</b>Xml texts4`);
    r7 = x7 + s5;
    assertEquality(r7, xml `<a>a</a><b>b</b>Xml texts5`);
    r7 = x7 + s6;
    assertEquality(r7, xml `<a>a</a><b>b</b>Xml texts6`);

    xml<xml:Comment|xml:ProcessingInstruction> x8 = xml `<!--comment--><?pi?>`;
    xml<xml:Comment|xml:ProcessingInstruction|xml:Text> r8 = x8 + s1;
    assertEquality(r8, xml `<!--comment--><?pi?>s1`);
    r8 = x8 + s2;
    assertEquality(r8, xml `<!--comment--><?pi?>s`);
    r8 = x8 + s3;
    assertEquality(r8, xml `<!--comment--><?pi?>s3`);
    r8 = x8 + s4;
    assertEquality(r8, xml `<!--comment--><?pi?>s4`);
    r8 = x8 + s5;
    assertEquality(r8, xml `<!--comment--><?pi?>s5`);
    r8 = x8 + s6;
    assertEquality(r8, xml `<!--comment--><?pi?>s6`);

    xml<xml:Comment|xml:Text> x9 = xml `<!--comment 2-->Xml text 2`;
    xml<xml:Comment|xml:Text> r9 = x9 + s1;
    assertEquality(r9, xml `<!--comment 2-->Xml text 2s1`);
    r9 = x9 + s2;
    assertEquality(r9, xml `<!--comment 2-->Xml text 2s`);
    r9 = x9 + s3;
    assertEquality(r9, xml `<!--comment 2-->Xml text 2s3`);
    r9 = x9 + s4;
    assertEquality(r9, xml `<!--comment 2-->Xml text 2s4`);
    r9 = x9 + s5;
    assertEquality(r9, xml `<!--comment 2-->Xml text 2s5`);
    r9 = x9 + s6;
    assertEquality(r9, xml `<!--comment 2-->Xml text 2s6`);

    xml<xml:ProcessingInstruction|xml:Text> x10 = xml `<?pi?>Xml text 3`;
    xml<xml:ProcessingInstruction|xml:Text> r10 = x10 + s1;
    assertEquality(r10, xml `<?pi?>Xml text 3s1`);
    r10 = x10 + s2;
    assertEquality(r10, xml `<?pi?>Xml text 3s`);
    r10 = x10 + s3;
    assertEquality(r10, xml `<?pi?>Xml text 3s3`);
    r10 = x10 + s4;
    assertEquality(r10, xml `<?pi?>Xml text 3s4`);
    r10 = x10 + s5;
    assertEquality(r10, xml `<?pi?>Xml text 3s5`);
    r10 = x10 + s6;
    assertEquality(r10, xml `<?pi?>Xml text 3s6`);

    xml<xml:Element|xml:Comment|xml:ProcessingInstruction> x11 = xml `<a>a</a><!--comment--><?pi?>`;
    xml<xml:Element|xml:Comment|xml:ProcessingInstruction|xml:Text> r11 = x11 + s1;
    assertEquality(r11, xml `<a>a</a><!--comment--><?pi?>s1`);
    r11 = x11 + s2;
    assertEquality(r11, xml `<a>a</a><!--comment--><?pi?>s`);
    r11 = x11 + s3;
    assertEquality(r11, xml `<a>a</a><!--comment--><?pi?>s3`);
    r11 = x11 + s4;
    assertEquality(r11, xml `<a>a</a><!--comment--><?pi?>s4`);
    r11 = x11 + s5;
    assertEquality(r11, xml `<a>a</a><!--comment--><?pi?>s5`);
    r11 = x11 + s6;
    assertEquality(r11, xml `<a>a</a><!--comment--><?pi?>s6`);

    xml<xml:Element|xml:Comment|xml:Text> x12 = xml `<a>a</a><!--comment-->text`;
    xml<xml:Element|xml:Comment|xml:Text> r12 = x12 + s1;
    assertEquality(r12, xml `<a>a</a><!--comment-->texts1`);
    r12 = x12 + s2;
    assertEquality(r12, xml `<a>a</a><!--comment-->texts`);
    r12 = x12 + s3;
    assertEquality(r12, xml `<a>a</a><!--comment-->texts3`);
    r12 = x12 + s4;
    assertEquality(r12, xml `<a>a</a><!--comment-->texts4`);
    r12 = x12 + s5;
    assertEquality(r12, xml `<a>a</a><!--comment-->texts5`);
    r12 = x12 + s6;
    assertEquality(r12, xml `<a>a</a><!--comment-->texts6`);

    xml<xml:Element|xml:ProcessingInstruction|xml:Text> x13 = xml `text<a>a</a><?data?>`;
    xml<xml:Element|xml:ProcessingInstruction|xml:Text> r13 = x13 + s1;
    assertEquality(r13, xml `text<a>a</a><?data?>s1`);
    r13 = x13 + s2;
    assertEquality(r13, xml `text<a>a</a><?data?>s`);
    r13 = x13 + s3;
    assertEquality(r13, xml `text<a>a</a><?data?>s3`);
    r13 = x13 + s4;
    assertEquality(r13, xml `text<a>a</a><?data?>s4`);
    r13 = x13 + s5;
    assertEquality(r13, xml `text<a>a</a><?data?>s5`);
    r13 = x13 + s6;
    assertEquality(r13, xml `text<a>a</a><?data?>s6`);

    xml<xml:Comment|xml:ProcessingInstruction|xml:Text> x14 = xml `<!--comment--><?pi?>text`;
    xml<xml:Comment|xml:ProcessingInstruction|xml:Text> r14 = x14 + s1;
    assertEquality(r14, xml `<!--comment--><?pi?>texts1`);
    r14 = x14 + s2;
    assertEquality(r14, xml `<!--comment--><?pi?>texts`);
    r14 = x14 + s3;
    assertEquality(r14, xml `<!--comment--><?pi?>texts3`);
    r14 = x14 + s4;
    assertEquality(r14, xml `<!--comment--><?pi?>texts4`);
    r14 = x14 + s5;
    assertEquality(r14, xml `<!--comment--><?pi?>texts5`);
    r14 = x14 + s6;
    assertEquality(r14, xml `<!--comment--><?pi?>texts6`);

    xml<never> x15 = xml ``;
    xml:Text r15 = x15 + s1;
    assertEquality(r15, xml `s1`);
    r15 = x15 + s2;
    assertEquality(r15, xml `s`);
    r15 = x15 + s3;
    assertEquality(r15, xml `s3`);
    r15 = x15 + s4;
    assertEquality(r15, xml `s4`);
    r15 = x15 + s5;
    assertEquality(r15, xml `s5`);
    r15 = x15 + s6;
    assertEquality(r15, xml `s6`);
}

function assertEquality(any actual, any expected) {
    if actual is anydata && expected is anydata && actual == expected {
        return;
    }
    panic error("expected '" + expected.toString() + "', found '" + actual.toString() + "'");
}
