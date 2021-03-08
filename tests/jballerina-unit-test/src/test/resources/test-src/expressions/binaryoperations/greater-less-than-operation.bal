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
    string? f = ();
    string? g = ();

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

    test:assertTrue(a > f);
    test:assertTrue(a >= f);
    test:assertFalse(a < f);
    test:assertFalse(a <= f);

    test:assertTrue(f < a);
    test:assertTrue(f <= a);
    test:assertFalse(f > a);
    test:assertFalse(f >= a);

    test:assertTrue(d > f);
    test:assertTrue(d >= f);
    test:assertFalse(d < f);
    test:assertFalse(d <= f);

    test:assertFalse(f > g);
    test:assertTrue(f >= g);
    test:assertFalse(f < g);
    test:assertTrue(f <= g);
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

function testArrayComparison() {
    int[] a = [12, 56, 2];
    int[] b = [13, 56, 2];
    int[] c = [12, 58, 2];
    int[] d = [12, 56, 2];
    int[] e = [];

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
}

function testTupleComparison() {
    [int, decimal] a = [59215, 9945];
    [int, decimal] b = [59283, 24345];
    [int, decimal] c = [59215, 24345];
    [int, decimal...] d = [59283, 24345, 12342];
    [int, decimal] e = [];

    test:assertTrue(a < b);
    test:assertTrue(a <= b);
    test:assertFalse(a > b);
    test:assertFalse(a >= b);

    test:assertFalse(c < a);
    test:assertFalse(c <= a);
    test:assertTrue(c > a);
    test:assertTrue(c >= a);

    test:assertFalse(b > d);
    test:assertFalse(b >= d);
    test:assertTrue(b < d);
    test:assertTrue(b <= d);

    test:assertFalse(d < e);
    test:assertFalse(d <= e);
    test:assertTrue(d > e);
    test:assertTrue(d >= e);
}

function testUnionComparison() {
    string|int a = 2;
    string|int b = 12;
    string|int c = 20;
    string|int d = 2;
    string|int? e = ();

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

    test:assertFalse(d < e);
    test:assertFalse(d <= e);
    test:assertTrue(d > e);
    test:assertTrue(d >= e);
}
