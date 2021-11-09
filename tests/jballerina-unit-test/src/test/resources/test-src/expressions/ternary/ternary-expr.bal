function test1 (int value) returns (int) {
    int x = value > 10 ? 15 : 5;
    return x;
}

function test2 (int value) returns (string) {
    var x = 10 <= value ? "large" : "small";
    return x;
}

function test3 (int value) returns (float) {
    float x = value == 10 ? 10.5 : 9.5;
    return x;
}

function test4 (int value) returns (string) {
    if (value == 20 ? true : false) {
        return "if";
    }
    return "else";
}

function test5 (int value) returns (string) {
    return foo(10, value == 10 ? "ten" : "other", value != 10);
}

function foo (int a, string b, boolean c) returns (string) {
    return a.toString() + b + c.toString();
}

type Person record {
    string name;
    string location?;
};

function test6 (string s) returns (string) {
    Person p = {name : s == "admin" ? "super" : "tom"};
    return p.name;
}

function test7 (string s) returns (int|error) {
    map<any> m = {"data" : s == "one" ? 1 : 2};
    var y = <int>m["data"];
    return y;
}

function test8 (string s) returns (string) {
    string x = string `hello ${s == "world" ? "world...!!" : "everyone..!"}`;
    return x;
}

function test9 (string s) returns (string) {
    return s == "a" ? bax() : bar();
}

function bax () returns (string) {
    return "bax";
}

function bar () returns (string) {
    return "bar";
}

function test10 (string s) returns (Person) {
    Person tom = {name : "tom", location : "US"};
    Person bob = {name : "bob", location : "UK"};
    return s == "tom" ? tom : bob;
}

function testNestedTernary1 (int value) returns [string, string] {
    string s1 = value > 70 ? "morethan70" : value > 40 ? "morethan40" : value > 20 ? "morethan20" : "lessthan20";
    string s2 = value > 70 ? "morethan70" : (value > 40 ? "morethan40" : (value > 20 ? "morethan20" : "lessthan20"));
    return [s1, s2];
}

function testNestedTernary2 (int value) returns [string, string] {
    string s1 = value > 40 ? value > 70 ? "morethan70" : "lessthan70" : value > 20 ? "morethan20" : "lessthan20";
    string s2 = value > 40 ? (value > 70 ? "morethan70" : "lessthan70") : (value > 20 ? "morethan20" : "lessthan20");
    return [s1, s2];
}

function testNestedTernary3 (int value) returns [string, string] {
    string s1 = value < 40 ? value > 20 ? value < 30 ? "lessthan30" : "morethan30" : "lessthan20" : value > 45 ? "morethan45" : "lessthan45";
    string s2 = value < 40 ? (value > 20 ? (value < 30 ? "lessthan30" : "morethan30") : "lessthan20") : (value > 45 ? "morethan45" : "lessthan45");
    return [s1, s2];
}

function testNestedTernary4 (int value) returns [string, string] {
    string s1 = value > 40 ? value > 70 ? "morethan70" : value > 50 ? "morethan50" : "lessthan50" : value > 20 ? "morethan20" : "lessthan20";
    string s2 = value > 40 ? (value > 70 ? "morethan70" : (value > 50 ? "morethan50" : "lessthan50")) : (value > 20 ? "morethan20" : "lessthan20");
    return [s1, s2];
}

function testNestedTernary5 (int value) returns [string, string] {
    string s1 = value > 50 ? "morethan50" : value > 30 ? value > 40 ? "morethan40" : "lessthan40" : value > 20 ? "morethan20" : "lessthan20";
    string s2 = value > 50 ? "morethan50" : (value > 30 ? (value > 40 ? "morethan40" : "lessthan40") : (value > 20 ? "morethan20" : "lessthan20"));
    return [s1, s2];
}

function testErrorInTernary() returns int|error {
    int x = 8;
    var result = x > 5 ? decrement(x) : increment(x);
    return result;
}

function decrement(int i) returns error|int {
    return i - 1;
}

function increment(int i) returns error|int {
    return i + 1;
}

function testPredeclPrefixInTernary() returns int {
    int a = true? int:sum(5, 6) : 5;
    return a;
}

function testTernaryAsArgument() {
    string[] filters = [];
    boolean trueVal = true;
    boolean falseVal = false;

    string s1 = trueVal ? "str" : "";
    filters.push(s1);
    filters.push(trueVal ? "str" : "");

    string s2 = falseVal ? "str" : "";
    filters.push(s2);
    filters.push(falseVal ? "str" : "");

    byte[] a = [];
    a.push(trueVal ? 0 : 255);
    a.push(falseVal ? 0 : 255);

    assertEquals(filters[0], "str");
    assertEquals(filters[2], "");
    assertEquals(filters[0], filters[1]);
    assertEquals(filters[2], filters[3]);
    assertEquals(a[0], 0);
    assertEquals(a[1], 255);
}

int|() x1 = ();
int y1 = 3000;
boolean cond = true;
string b1 = (cond? x1 : y1).toBalString();
string b2 = (cond? y1 : x1).toString();

// Following lines should be uncommented after fixing 'ballerina-lang #33678'
// int x2 = 123;
// string y2 = "a";
// string b3 = (cond? x2 : y2).toBalString();
// string b4 = (cond? y2 : x2).toString();
// string b5 = (cond? (cond? (cond? y2 : x2) : x2) : x2).toString();
// string b6 = (cond? (cond? x2 : (cond? x2 : y2)) : (cond? x2 : y2)).toString();

// int x3 = -9223372036854775807;
// float y3 = 2.12;
// string b7 = (cond? x3 : y3).toBalString();
// string b8 = (cond? y3 : x3).toString();

// decimal x4 = 123.12;
// float y4 = 211.43;
// string b9 = (cond? x4 : y4).toBalString();
// string b10 = (cond? y4 : x4).toString();

// int x5 = 123;
// byte y5 = 250;
// string b11 = (cond? x5 : y5).toBalString();
// string b12 = (cond? y5 : x5).toString();

// int x6 = 123;
// int:Signed16 y6 = -32000;
// string b13 = (cond? x6 : y6).toBalString();
// string b14 = (cond? y6 : x6).toString();

// int:Unsigned8 x7 = 223;
// int:Signed8 y7 = -128;
// string b15 = (cond? x7 : y7).toBalString();
// string b16 = (cond? y7 : x7).toString();
// string b17 = (cond? (cond? (cond? y7 : x7) : x7) : x7).toBalString();
// string b18 = (cond? (cond? x4 : (cond? y1 : x3)) : (cond? y7 : x7)).toString();

// string x8 = "abc";
// string:Char y8 = "a";
// string b19 = (cond? x8 : y8).toString();
// string b20 = (cond? y8 : x8).toString();
// string b21 = (cond? (cond? (cond? y1 : x3) : y8) : x8).toBalString();
// string b22 = (cond? (cond? x3 : (cond? y6 : x7)) : (cond? y8 : x8)).toString();

// int x9 = 123;
// json y9 = "a";
// string b23 = (cond? x9 : y9).toBalString();
// string b24 = (cond? y9 : x9).toString();

function testTernaryWithLangValueMethodCallsModuleLevel() {
    assertEquals("()", b1);
    assertEquals("3000", b2);

    // Following lines should be uncommented after fixing 'ballerina-lang #33678'
    // assertEquals("()", b1);
    // assertEquals("3000", b2);
    // assertEquals("123", b3);
    // assertEquals("a", b4);
    // assertEquals("a", b5);
    // assertEquals("123", b6);
    // assertEquals("-9223372036854775807", b7);
    // assertEquals("2.12", b8);
    // assertEquals("123.12d", b9);
    // assertEquals("211.43", b10);
    // assertEquals("123", b11);
    // assertEquals("250", b12);
    // assertEquals("123", b13);
    // assertEquals("-32000", b14);
    // assertEquals("223", b15);
    // assertEquals("-128", b16);
    // assertEquals("-128", b17);
    // assertEquals("123.12", b18);
    // assertEquals("abc", b19);
    // assertEquals("a", b20);
    // assertEquals("3000", b21);
    // assertEquals("-9223372036854775807", b22);
    // assertEquals("123", b23);
    // assertEquals("a", b24);
}

function testTernaryWithLangValueMethodCalls() {
    int|() x10 = ();
    int y10 = 3000;

    string b25 = (cond? x10 : y10).toBalString();
    assertEquals("()", b25);
    string b26 = (cond? y10 : x10).toString();
    assertEquals("3000", b26);

    int x11 = 123;
    string y11 = "a";

    string b27 = (cond? x11 : y11).toBalString();
    assertEquals("123", b27);
    string b28 = (cond? y11 : x11).toString();
    assertEquals("a", b28);
    string b29 = (cond? (cond? (cond? y11 : x11) : x11) : x11).toString();
    assertEquals("a", b29);
    string b30 = (cond? (cond? x11 : (cond? x11 : y11)) : (cond? x11 : y11)).toString();
    assertEquals("123", b30);

    int x12 = -9223372036854775807;
    float y12 = 2.12;

    string b31 = (cond? x12 : y12).toBalString();
    assertEquals("-9223372036854775807", b31);
    string b32 = (cond? y12 : x12).toString();
    assertEquals("2.12", b32);

    decimal x13 = 123.12;
    float y13 = 211.43;

    string b33 = (cond? x13 : y13).toBalString();
    assertEquals("123.12d", b33);
    string b34 = (cond? y13 : x13).toString();
    assertEquals("211.43", b34);

    int x14 = 123;
    byte y14 = 250;

    string b35 = (cond? x14 : y14).toBalString();
    assertEquals("123", b35);
    string b36 = (cond? y14 : x14).toString();
    assertEquals("250", b36);

    int x15 = 123;
    int:Signed16 y15 = -32000;

    string b37 = (cond? x15 : y15).toBalString();
    assertEquals("123", b37);
    string b38 = (cond? y15 : x15).toString();
    assertEquals("-32000", b38);

    int:Unsigned8 x16 = 223;
    int:Signed8 y16 = -128;

    string b39 = (cond? x16 : y16).toBalString();
    assertEquals("223", b39);
    string b40 = (cond? y16 : x16).toString();
    assertEquals("-128", b40);
    string b41 = (cond? (cond? (cond? y16 : x14) : x16) : x16).toBalString();
    assertEquals("-128", b41);
    string b42 = (cond? (cond? x15 : (cond? y12 : x14)) : (cond? y16 : x16)).toString();
    assertEquals("123", b42);

    string x17 = "abc";
    string:Char y17 = "a";

    string b43 = (cond? x17 : y17).toString();
    assertEquals("abc", b43);
    string b44 = (cond? y17 : x17).toString();
    assertEquals("a", b44);
    string b45 = (cond? (cond? (cond? y10 : x12) : y17) : x17).toBalString();
    assertEquals("3000", b45);
    string b46 = (cond? (cond? x12 : (cond? y15 : x16)) : (cond? y17 : x17)).toString();
    assertEquals("-9223372036854775807", b46);

    int x18 = 123;
    json y18 = "a";

    string b47 = (cond? x18 : y18).toBalString();
    assertEquals("123", b47);
    string b48 = (cond? y18 : x18).toString();
    assertEquals("a", b48);
}

const ASSERTION_ERROR_REASON = "AssertionError";

function assertEquals(anydata expected, anydata actual) {
    if expected == actual {
        return;
    }

    typedesc<anydata> expT = typeof expected;
    typedesc<anydata> actT = typeof actual;
    string msg = "expected [" + expected.toString() + "] of type [" + expT.toString()
                            + "], but found [" + actual.toString() + "] of type [" + actT.toString() + "]";
    panic error(ASSERTION_ERROR_REASON, message = msg);
}
