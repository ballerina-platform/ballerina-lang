import ballerina/lang.test as test;

function testIntRanges(int a) returns (int) {
    int retunType = 0;
    if (a <= 0) {
        retunType = 1;
    } else if ((a > 0) && (a < 100)) {
        retunType = 2;
    } else if (a >= 100) {
        retunType = 3;
    }
    return retunType;
}

function testFloatRanges(float a) returns (int) {
    int retunType = 0;
    if (a <= 0.0) {
        retunType = 1;
    } else if ((a > 0.0) && (a < 100.0)) {
        retunType = 2;
    } else if (a >= 101.0) {
        retunType = 3;
    }
    return retunType;
}

function testIntAndFloatCompare(int a, float b) returns (boolean) {
    return a > b;
}

function intGTFloat(int a, float b) returns (boolean) {
    return a > b;
}

function floatGTInt(float a, int b) returns (boolean) {
    return a > b;
}

function intLTFloat(int a, float b) returns (boolean) {
    return a < b;
}

function floatLTInt(float a, int b) returns (boolean) {
    return a < b;
}

function intLTEFloat(int a, float b) returns (boolean) {
    return a <= b;
}

function floatLTEInt(float a, int b) returns (boolean) {
    return a <= b;
}

function intGTEFloat(int a, float b) returns (boolean) {
    return a >= b;
}

function floatGTEInt(float a, int b) returns (boolean) {
    return a >= b;
}

function testDecimalComparison() {
    decimal lowValue = 3;
    decimal highValue = 5;

    test:assertTrue(lowValue < highValue);
    test:assertTrue(lowValue <= highValue);

    test:assertTrue(highValue > lowValue);
    test:assertTrue(highValue >= lowValue);

    test:assertFalse(highValue < lowValue);
    test:assertFalse(highValue <= lowValue);
    test:assertFalse(lowValue > highValue);
    test:assertFalse(lowValue >= highValue);

    test:assertFalse(lowValue > lowValue);
    test:assertFalse(lowValue < lowValue);
    test:assertTrue(lowValue >= lowValue);
    test:assertTrue(lowValue <= lowValue);
}

function testStringComparison() {
    string a = "abc";
    string b = "abcd";
    string c = "abc";
    string d = "";
    string e = "";

    test:assertTrue(a < b);
    test:assertTrue(a <= b);
    test:assertFalse(a > b);
    test:assertFalse(a >= b);

    test:assertTrue(b > a);
    test:assertTrue(b >= a);
    test:assertFalse(b < a);
    test:assertFalse(b <= a);

    test:assertFalse(a > c);
    test:assertTrue(a >= c);
    test:assertFalse(a < c);
    test:assertTrue(a <= c);

    test:assertTrue(a > d);
    test:assertTrue(a >= d);
    test:assertFalse(a < d);
    test:assertFalse(a <= d);

    test:assertFalse(d > e);
    test:assertTrue(d >= e);
    test:assertFalse(d < e);
    test:assertTrue(d <= e);
}

function testBooleanComparison() {
    boolean a = true;
    boolean b = false;
    boolean c = true;
    boolean d = false;

    test:assertFalse(a < b);
    test:assertFalse(a <= b);
    test:assertTrue(a > b);
    test:assertTrue(a >= b);

    test:assertTrue(b < a);
    test:assertTrue(b <= a);
    test:assertFalse(b > a);
    test:assertFalse(b >= a);

    test:assertFalse(a > c);
    test:assertTrue(a >= c);
    test:assertFalse(a < c);
    test:assertTrue(a <= c);

    test:assertFalse(b > d);
    test:assertTrue(b >= d);
    test:assertFalse(b < d);
    test:assertTrue(b <= d);
}

function testArrayComparison1() {
    int[] a = [12, 56, 2];
    int[] b = [13, 56, 2];
    int[] c = [12, 58, 2];
    int[] d = [12, 56, 2];
    int[] e = [];
    int[] f = [];

    test:assertTrue(a < b);
    test:assertTrue(a <= b);
    test:assertFalse(a > b);
    test:assertFalse(a >= b);

    test:assertFalse(c < a);
    test:assertFalse(c <= a);
    test:assertTrue(c > a);
    test:assertTrue(c >= a);

    test:assertFalse(a > d);
    test:assertTrue(a >= d);
    test:assertFalse(a < d);
    test:assertTrue(a <= d);

    test:assertFalse(a < e);
    test:assertFalse(a <= e);
    test:assertTrue(a > e);
    test:assertTrue(a >= e);

    test:assertFalse(e > f);
    test:assertTrue(e >= f);
    test:assertFalse(e < f);
    test:assertTrue(e <= f);
}

function testArrayComparison2() {
    float[] a = [+0.0, 23.1];
    float[] b = [-0.0, 23.1];
    float?[] c = [12.34, ()];
    float?[] d = [12.34, ()];

    test:assertFalse(a < b);
    test:assertTrue(a <= b);
    test:assertFalse(a > b);
    test:assertTrue(a >= b);

    test:assertFalse(c < d);
    test:assertTrue(c <= d);
    test:assertFalse(c > d);
    test:assertTrue(c >= d);
}

function testArrayComparison3() {
    (string|int)[] a = [12, "DEF"];
    (string|int)[] b = [13, "ABC"];
    (string|int)[] c = [12, "ABC"];
    (string|int)[] d = [];

    test:assertTrue(a < b);
    test:assertTrue(a <= b);
    test:assertFalse(a > b);
    test:assertFalse(a >= b);

    test:assertFalse(a < c);
    test:assertFalse(a <= c);
    test:assertTrue(a > c);
    test:assertTrue(a >= c);

    test:assertFalse(a < d);
    test:assertFalse(a <= d);
    test:assertTrue(a > d);
    test:assertTrue(a >= d);
}

function testTupleComparison1() {
    [int, decimal] a = [59215, 9945];
    [int, decimal] b = [59283, 24345];
    [int, decimal] c = [59215, 24345];
    [int, decimal...] d = [59283, 24345, 12342];
    [int, decimal...] e = [59283, 24345, 123425];
    [int, decimal] f = [];
    [int, decimal] g = [];

    test:assertTrue(a < b);
    test:assertTrue(a <= b);
    test:assertFalse(a > b);
    test:assertFalse(a >= b);

    test:assertFalse(c < a);
    test:assertFalse(c <= a);
    test:assertTrue(c > a);
    test:assertTrue(c >= a);

    test:assertTrue(d < e);
    test:assertTrue(d <= e);
    test:assertFalse(d > e);
    test:assertFalse(d >= e);

    test:assertFalse(f < g);
    test:assertTrue(f <= g);
    test:assertFalse(f > g);
    test:assertTrue(f >= g);
}

function testTupleComparison2() {
    [int, decimal, boolean, byte, float, string] a = [59215, 9945, true, 234, 123.45, "ABC"];
    [int, decimal, boolean, byte, float, string] b = [59215, 890, true, 234, 123.45, "ABC"];
    [int, decimal, boolean, byte, float, string] c = [59215, 9945, false, 234, 123.45, "ABC"];
    [int, decimal, boolean, byte, float, string] d = [59215, 9945, true, 123, 123.45, "ABC"];
    [int, decimal, boolean, byte, float, string] e = [59215, 9945, true, 234, 12.43, "ABC"];
    [int, decimal, boolean, byte, float, string] f = [59215, 9945, true, 234, 123.45, "LMN"];

    test:assertFalse(a < b);
    test:assertFalse(a <= b);
    test:assertTrue(a > b);
    test:assertTrue(a >= b);

    test:assertTrue(c < a);
    test:assertTrue(c <= a);
    test:assertFalse(c > a);
    test:assertFalse(c >= a);

    test:assertTrue(a > d);
    test:assertTrue(a >= d);
    test:assertFalse(a < d);
    test:assertFalse(a <= d);

    test:assertTrue(a > e);
    test:assertTrue(a >= e);
    test:assertFalse(a < e);
    test:assertFalse(a <= e);

    test:assertTrue(f > a);
    test:assertTrue(f >= a);
    test:assertFalse(f < a);
    test:assertFalse(f <= a);
}

function testUnionComparison() {
    string|int a = 2;
    string|int b = 12;
    string|int c = 20;
    string|int d = 2;

    test:assertTrue(a < b);
    test:assertTrue(a <= b);
    test:assertFalse(a > b);
    test:assertFalse(a >= b);

    test:assertFalse(c < a);
    test:assertFalse(c <= a);
    test:assertTrue(c > a);
    test:assertTrue(c >= a);

    test:assertFalse(a > d);
    test:assertTrue(a >= d);
    test:assertFalse(a < d);
    test:assertTrue(a <= d);
}

type Utc readonly & [int,decimal];

function testTypeComparison() {
    Utc a = [59215, 9945];
    Utc b = [59283, 24345];

    test:assertTrue(a < b);
    test:assertTrue(a <= b);
    test:assertFalse(a > b);
    test:assertFalse(a >= b);
}

function testUnorderedTypeComparison1() {
    [int, string?] a = [59215, "ABC"];
    [int, string?] b = [59215, ()];

    boolean x = a > b;
}

function testUnorderedTypeComparison2() {
    [int, float] a = [59215, (0.0/0.0)];
    [int, float] b = [59215, 123.432];

    boolean x = a > b;
}

function testUnorderedTypeComparison3() {
    [int, float] a = [59215, (0.0/0.0)];
    [int, float] b = [59215, (0.0/0.0)];

    boolean x = a > b;
}

function testUnorderedTypeComparison4() {
    string? a = ();
    string? b = "ABC";

    boolean x = a > b;
}

function testUnorderedTypeComparison5() {
    int? a = 400;
    int? b = ();

    boolean x = a > b;
}

function testUnorderedTypeComparison6() {
    float a = 400.123;
    float b = (0.0/0.0);

    boolean x = a > b;
}

function testUnorderedTypeComparison7() {
    float a = (0.0/0.0);
    float b = (0.0/0.0);

    boolean x = a > b;
}

function testUnorderedTypeComparison8() {
    float a = (0.0/0.0);
    float b = (0.0/0.0);

    boolean x = a > b;
}
