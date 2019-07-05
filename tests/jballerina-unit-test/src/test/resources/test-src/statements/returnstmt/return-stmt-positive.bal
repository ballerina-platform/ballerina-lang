function testReturn() {
    return;
}

function testReturnOneVarDcl() {
    int x = 0;

    return;
}

function testReturnOneReturnArg() returns (int) {
    return 5;
}

function testReturnOneParam(int a) {
    return;
}

function testReturnOneParamOneReturnArg(int a) returns (int) {
    return a;
}

function testReturnOneParamOneVarDclOneReturnArg(int a) returns (int) {
    int b = 10;

    return a + b;
}

function testReturnNoParamTwoVarDclsTwoReturnArgs() returns [int, string] {
    int b = 10;
    string t = "john";

    return [b, t];
}

function testReturnTwoVarDclsTwoReturnArgs(int a, int q) returns [int, string] {
    int b = a + q;
    string t = "john";

    return [a + b, t];
}

function testReturnThreeVarDclsThreeReturnArgs(int a, int q) returns [int, string, float] {
    int b = a + q;
    string t = "john";
    float f = 0.5 * 2.0;

    return [a + b, t, f];
}


function splitUtil(string s) returns [string, string, string] {
    return ["section1", "section2", "section3"];
}

function testSplitString() returns [string, string, string] {
    string section = "Ballerina is a programming language";
    return splitUtil(section);
}


function testToUpperUtil(string s) returns (string) {
    // Covert s to upper
    return "SECTION";
}

function testToUpperUtilDouble(string s1, string s2) returns [string, string] {
    // Covert s to upper
    string s3 = "NAME1";
    string s4 = "NAME2";
    return [s3, s4];
}

function testToUpper(string s) returns (string) {
    return testToUpperUtil(s);
}

function testToUpper1(string s1, string s2) returns [string, string] {
    return [testToUpperUtil(s1), s2];
}

function testToUpper2(string s1, string s2) returns [string, string] {
    return [s1, testToUpperUtil(s2)];
}

function testToUpper3(string s1, string s2) returns [string, string] {
    return [testToUpperUtil(s1), testToUpperUtil(s2)];
}

function testToUpper4(string s1, string s2) returns [string, string] {
    return testToUpperUtilDouble(s1, s2);
}

function testReturnWithThreeArguments() returns [int, string, int] {
    int x = 10;

    return [x, foo(1, 4), bar(5, "john", 6)];
}

function foo(int a, int b) returns (string) {
    return "foo";
}

function bar(int a, string s, int b) returns (int) {
    return 4;
}
