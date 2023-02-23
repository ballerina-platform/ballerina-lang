function testElvisValueTypeNested () returns (int) {
    int|() x = ();
    int|() y = 3000;
    int b;
    b = x ?: y ?: x;
    return b;
}

function testElvisValueTypeNotMatchingTypeWithRHS () returns (int) {
    int|() x = 120;
    int b;
    b = x ?: "string";
    return b;
}

function testElvisValueTypeNotMatchingTypeWithLHS () returns (int) {
    string|() x = ();
    int b;
    b = x ?: 3;
    return b;
}

function testElvisAsArgumentNegative() {
    int[] a = [];
    int? b = 1;
    a.push(b ?: "");

    byte[] c = [];
    byte? d = ();
    c.push(d ?: 256);
}

int? x1 = ();
byte? y1 = 255;
() z1 = ();
var elvisOutput1 = x1 ?: y1 ?: z1;
int v1 = elvisOutput1;

int? x2 = ();
int:Unsigned8? y2 = 255;
int:Signed8? z2 = -128;
var elvisOutput2 = x2 ?: y2 ?: z2;
var elvisOutput3 = x2 ?: y2 ?: x1 ?: y1 ?: z2;
int v2 = elvisOutput2;
int v3 = elvisOutput3;

decimal? x3 = ();
float? y3 = 1.213123;
int:Signed16? z3 = -128;
var elvisOutput4 = x3 ?: y3 ?: z3;
var elvisOutput5 = x3 ?: y3 ?: x2 ?: y2 ?: x1 ?: y1 ?: z3;
int v4 = elvisOutput4;
int v5 = elvisOutput5;

int? x4 = ();
string? y4 = ();
string:Char? z4 = ();
string[] z5 = ["a"];
var elvisOutput6 = x4 ?: y4 ?: z4 ?: z5;
int v6 = elvisOutput6;

function testNestedElvisNegative() {
    int? x5 = ();
    byte? y5 = ();
    () z6 = ();
    var elvisOutput7 = x5 ?: y5 ?: z6;
    int v7 = elvisOutput7;

    int? x6 = ();
    int:Unsigned8? y6 = ();
    int:Signed8? z7 = -128;
    var elvisOutput8 = x6 ?: y6 ?: z7;
    var elvisOutput9 = x6 ?: y6 ?: x5 ?: y5 ?: z6;
    int v8 = elvisOutput8;
    int v9 = elvisOutput9;

    decimal? x7 = ();
    float? y7 = ();
    int:Signed8? z8 = -128;
    var elvisOutput10 = x7 ?: y7 ?: z8;
    var elvisOutput11 = x7 ?: y7 ?: x6 ?: y6 ?: x5 ?: y5 ?: z8;
    int v10 = elvisOutput10;
    int v11 = elvisOutput11;

    int? x8 = ();
    string? y8 = "b";
    string:Char? z9 = "c";
    string[] z10 = ["a"];
    var elvisOutput12 = x8 ?: y8 ?: z9 ?: z10;
    int v12 = elvisOutput12;
}

type T1 json;

function testElvisWithJsonNegative() {
    json resp = ();
    int _ = (resp.population?.value ?: 0) / 1000000;
}

function testElvisWithJsonNegative2() {
    json a = {
        x: {
            y: 3,
            z: "a",
            t: true
        }
    };
    int _ = (a.x?.y ?: 0) / 10;
    string _ = (a.x?.z ?: "") + "b";
    boolean _ = (a.x?.t ?: true) && false;
}

function testElvisWithJsonNegative3() {
    T1 resp = ();
    int _ = (resp.population?.value ?: 0) / 1000000;
}

function testElvisWithJsonNegative4() {
    json a = ();
    any[] b = [];
    int _ = (a.x?.y ?: b) / 10;
}

function testElvisWithJsonNegative5() {
    T1 a = ();
    any[] b = [];
    int _ = (a.x?.y ?: b) / 10;
}

type NonOptionalType int[]|string;

function testInvalidElvisExpr(NonOptionalType i) {
    int[] _ = i ?: [1, 2]; // error
    int[]|string j = "str";
    int[] _ = j ?: [1, 2, 3]; // error
}
