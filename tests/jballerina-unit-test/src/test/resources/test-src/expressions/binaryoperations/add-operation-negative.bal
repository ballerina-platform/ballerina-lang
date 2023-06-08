function addJson() {
    json j1;
    json j2;
    json j3;
    j1 = {"name":"Jack"};
    j2 = {"state":"CA"};
    // Following line is invalid.
    j3 = j1 + j2;
}

function addIncompatibleTypes() {
    int i;
    // Following line is invalid.
    i = 5 + "abc";
}

const A = 10;
const B = 20;

type C A|B;

function addIncompatibleTypes2() {
    C a = 10;
    string b = "ABC";
    float|int c = 12;
    xml e = xml `abc`;

    int i1 = a + b;
    int i2 = a + c;
    xml i3 = a + e;
}

const E = "ABC";

type D A|E;

type F "ABC"|"CDE";

type G "ABC"|10;

function addIncompatibleTypes3() {
    D a = 10;
    int b = 12;
    F c = "ABC";
    G d = 10;

    int i1 = a + b;
    int i2 = c + b;
    int i3 = d + b;
}

function testImplicitConversion() {
    float a = 1;
    decimal b = 1;
    int c = 1;
    var x1 = a + b;
    any x2 = a + b;
    var x3 = a + c;
    any x4 = b + c;
    anydata x5 = c + a;

    C d = 10;
    float e = 12.25;
    var y1 = d + e;
    any y2 = d + e;

    I i = "i";
    O o = "o";
    xml x = xml `<foo>foo</foo><?foo?>text1<!--comment-->`;
    string y = "abc";

    string _ = x + y;
    FO _ = i + o;
}

type FO "fo";

type I "i";

type O "o";

type Strings "A"|"foo";

function errorFunction(string:Char|xml a, xml:Element|Strings b, xml<xml:Comment|xml:Text>|string c) {
    xml _ = a + a; 
    xml _ = a + b; 
    xml _ = a + c; 
    xml _ = b + b; 
    xml _ = b + c; 
    xml _ = c + c; 
}

function errorFunction2(int|float a, float|int b, byte|float c) {
    int _ = a + a; 
    int _ = a + b; 
    int _ = a + c; 
    int _ = b + b; 
    int _ = b + c; 
    int _ = c + c; 
}

function errorFunction3(decimal|float a, decimal|float b) {
    decimal _ = a + a; 
    decimal _ = a + b; 
    decimal _ = b + b; 
}

type ints 1|2;

function errorFunction4(int|float a, ints|float b) {
    int _ = a + a; 
    int _ = a + b;  
    int _ = b + b; 
}

function testAdditiveExprWithUnionOperandBelongingToSingleBasicTypeNegative() {
    string|("a"|"b") a = "a";
    _ = a + 1;
    _ = 1 + a;
    _ = a + a; // OK

    ("a"|"b")|string b = "a";
    _ = b + 1;
    _ = 1 + b;
    _ = b + b; // OK

    string|string:Char c = "s";
    _ = c + 1;
    _ = 1f + c;
    _ = c + c; // OK

    string|string d = "";
    _ = d + 1;
    _ = 1 + d;
    _ = d + d; // OK

    (1|2)|int e = 1;
    _ = e + "";
    _ = "" + e;
    _ = e + e; // OK

    xml:Element|xml:Text f = xml ``;
    _ = f + 1;
    _ = 1 + f;
    _ = f + f; // OK

    _ = a + e;
    _ = a + f; // OK
    _ = e + a;
    _ = e + f;
    _ = f + a; // OK
    _ = f + e;
}

function testMissingBinaryOp() {
    _ = "a""b";
    _ = bar()();
    _ = foo(("a""b") + 1);
    _ = foo(("a""b") + "c");

    int x = 2;
    int y = 3;
    _ = x y;
    _ = 1.5"b";
    _ = true y;
    _ = "a"x;
}

function foo(string s) returns string {
    return s;
}

function bar() {}