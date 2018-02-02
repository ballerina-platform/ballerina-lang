function testReturn() {
    return;
}

function testReturnOneVarDcl() {
    int x;

    x = 0;
    return;
}

function testReturnOneReturnArg() (int) {
    return 5;
}

function testReturnOneParam(int a) {
    return;
}

function testReturnOneParamOneReturnArg(int a) (int) {
    return a;
}

function testReturnOneParamOneVarDclOneReturnArg(int a) (int) {
    int b;

    b = 10;
    return a + b;
}

function testReturnTwoVarDclsTwoReturnArgs() (int, string) {
    int b;
    string t;

    b = 10;
    t = "john";

    return b, t;
}

function testReturnTwoVarDclsTwoReturnArgs(int a, int q) (int, string) {
    int b;
    string t;

    b = a + q;
    t = "john";

    return a + b, t;
}

function testReturnThreeVarDclsThreeReturnArgs(int a, int q) (int, string, float) {
    int b;
    string t;
    float f;

    b = a + q;
    t = "john";
    f = 0.5 * 2.0;

    return a + b, t, f;
}


function splitUtil(string s) (string part1, string part2, string part3) {
    return "section1", "section2", "section3";
}

function testSplitString() (string part1, string part2, string part3) {
    string section;

    section = "Ballerina is a programming language";
    return splitUtil(section);
}


function testToUpperUtil(string s) (string) {
    return "SECTION";
}

function testToUpperUtilDouble(string s1, string s2) (string s3, string s4) {
    s3 = "NAME1";
    s4 = "NAME2";
    return s3, s4;
}

function testToUpper(string s) (string) {
    return testToUpperUtil(s);
}

function testToUpper1(string s1, string s2) (string, string) {
    return testToUpperUtil(s1), s2;
}

function testToUpper2(string s1, string s2) (string, string) {
    return s1, testToUpperUtil(s2);
}

function testToUpper3(string s1, string s2) (string, string) {
    return testToUpperUtil(s1), testToUpperUtil(s2);
}

function testToUpper4(string s1, string s2) (string, string) {
    return testToUpperUtilDouble(s1, s2);
}

function testReturnWithThreeArguments() (int, string, int) {
    int x;
    x = 10;

    return x, foo(1, 4), bar(5, "john", 6);
}

function foo(int a, int b) (string) {
    return "foo";
}

function bar(int a, string s, int b) (int) {
    return 4;
}

