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

function compareByteValues(byte lowValue, byte highValue) {
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

function testByteComparison() {
    compareByteValues(0x20, 0x21);
    compareByteValues(32, 45);
    compareByteValues(0x20, 35);
    compareByteValues(0, 0xFF);
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

    test:assertTrue(e < a);
    test:assertTrue(e <= a);
    test:assertFalse(e > a);
    test:assertFalse(e >= a);

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
    float[] a = [10, 23.1, 30.1];
    float[] b = [10, 23.1];
    float[] c = [];

    test:assertFalse(a < b);
    test:assertFalse(a <= b);
    test:assertTrue(a > b);
    test:assertTrue(a >= b);

    test:assertTrue(b < a);
    test:assertTrue(b <= a);
    test:assertFalse(b > a);
    test:assertFalse(b >= a);

    test:assertTrue(c < a);
    test:assertTrue(c <= a);
    test:assertFalse(c > a);
    test:assertFalse(c >= a);
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

function testTupleComparison3() {
    [float, int] a = [10, 23];
    [float, int, string...] b = [10, 23, "ABC"];
    [float, int, string...] c = [10, 46];

    test:assertTrue(a < b);
    test:assertTrue(a <= b);
    test:assertFalse(a > b);
    test:assertFalse(a >= b);

    test:assertTrue(a < c);
    test:assertTrue(a <= c);
    test:assertFalse(a > c);
    test:assertFalse(a >= c);

    test:assertTrue(b < c);
    test:assertTrue(b <= c);
    test:assertFalse(b > c);
    test:assertFalse(b >= c);
}

function testTupleComparison4() {
    [float, int, string] a = [10, 23];
    [float, int, string...] b = [10, 23, "ABC"];

    test:assertTrue(a < b);
    test:assertTrue(a <= b);
    test:assertFalse(a > b);
    test:assertFalse(a >= b);

    test:assertFalse(b < a);
    test:assertFalse(b <= a);
    test:assertTrue(b > a);
    test:assertTrue(b >= a);
}

type Utc readonly & [int,decimal];

function testTypeComparison1() {
    Utc a = [59215, 9945];
    Utc b = [59283, 24345];

    test:assertTrue(a < b);
    test:assertTrue(a <= b);
    test:assertFalse(a > b);
    test:assertFalse(a >= b);
}

const float ONE = 1.0;
const float TWO = 2.0;

type OneOrTwo ONE|TWO;

function testTypeComparison2() {
    OneOrTwo[] a = [1, 2];
    float[] b = [3, 2];

    test:assertTrue(a < b);
    test:assertTrue(a <= b);
    test:assertFalse(a > b);
    test:assertFalse(a >= b);
}

type NumberSet 1|2|3|4|5;

function testTypeComparison3() {
    NumberSet[] a = [1, 2];
    int[] b = [3, 2];

    test:assertTrue(a < b);
    test:assertTrue(a <= b);
    test:assertFalse(a > b);
    test:assertFalse(a >= b);
}

function testTypeComparison4() {
    NumberSet a = 1;
    NumberSet b = 2;

    test:assertTrue(a < b);
    test:assertTrue(a <= b);
    test:assertFalse(a > b);
    test:assertFalse(a >= b);
}

function testTypeComparison5() {
    OneOrTwo a = 1;
    OneOrTwo b = 2;

    test:assertTrue(a < b);
    test:assertTrue(a <= b);
    test:assertFalse(a > b);
    test:assertFalse(a >= b);
}

type TenOrEleven 10|11;

function testTypeComparison6() {
    TenOrEleven a = 10;
    NumberSet b = 2;

    test:assertFalse(a < b);
    test:assertFalse(a <= b);
    test:assertTrue(a > b);
    test:assertTrue(a >= b);

    test:assertTrue(b < a);
    test:assertTrue(b <= a);
    test:assertFalse(b > a);
    test:assertFalse(b >= a);
}

type TwoFloats 50.6|32.5;

function testTypeComparison7() {
    OneOrTwo a = 2.0;
    TwoFloats b = 32.5;

    test:assertTrue(a < b);
    test:assertTrue(a <= b);
    test:assertFalse(a > b);
    test:assertFalse(a >= b);

    test:assertFalse(b < a);
    test:assertFalse(b <= a);
    test:assertTrue(b > a);
    test:assertTrue(b >= a);
}

function testTypeComparison8() {
    float a = 2.0;
    TwoFloats b = 32.5;

    test:assertTrue(a < b);
    test:assertTrue(a <= b);
    test:assertFalse(a > b);
    test:assertFalse(a >= b);

    test:assertFalse(b < a);
    test:assertFalse(b <= a);
    test:assertTrue(b > a);
    test:assertTrue(b >= a);
}

function testTypeComparison9() {
    float? a = 2.0;
    TwoFloats? b = 32.5;

    test:assertTrue(a < b);
    test:assertTrue(a <= b);
    test:assertFalse(a > b);
    test:assertFalse(a >= b);
}

function testUnionComparison1() {
    int? a = 1;
    int? b = 2;

    test:assertTrue(a < b);
    test:assertTrue(a <= b);
    test:assertFalse(a > b);
    test:assertFalse(a >= b);
}

type DecimalOrNil decimal?;

function testUnionComparison2() {
    DecimalOrNil a = 12d;
    DecimalOrNil b = 24d;

    test:assertTrue(a < b);
    test:assertTrue(a <= b);
    test:assertFalse(a > b);
    test:assertFalse(a >= b);
}

function testUnionComparison3() {
    int|int:Signed32 a = 1;
    int|int:Signed32 b = 2;

    test:assertTrue(a < b);
    test:assertTrue(a <= b);
    test:assertFalse(a > b);
    test:assertFalse(a >= b);
}

function testUnionComparison4() {
    string|string:Char a = "A";
    string|string:Char b = "B";

    test:assertTrue(a < b);
    test:assertTrue(a <= b);
    test:assertFalse(a > b);
    test:assertFalse(a >= b);
}

function testUnionComparison5() {
    OneOrTwo? a = 2.0;
    TwoFloats? b = 32.5;

    test:assertTrue(a < b);
    test:assertTrue(a <= b);
    test:assertFalse(a > b);
    test:assertFalse(a >= b);

    TwoFloats? c = ();

    test:assertFalse(c < a);
    test:assertFalse(c <= a);
    test:assertFalse(c > a);
    test:assertFalse(c >= a);
}

function testUnionComparison6() {
    int|(int|int|int) a = 1;
    int b = 2;
    int|int:Signed32|int:Signed16 c = 4;

    test:assertTrue(a < b);
    test:assertTrue(a <= b);
    test:assertFalse(a > b);
    test:assertFalse(a >= b);

    test:assertTrue(b < c);
    test:assertTrue(b <= c);
    test:assertFalse(b > c);
    test:assertFalse(b >= c);
}

function testUnionComparison7() {
    TwoFloats a = 50.6;
    float b = 2.0;
    TwoFloats|OneOrTwo c = 1.0;

    test:assertFalse(a < b);
    test:assertFalse(a <= b);
    test:assertTrue(a > b);
    test:assertTrue(a >= b);

    test:assertFalse(b < c);
    test:assertFalse(b <= c);
    test:assertTrue(b > c);
    test:assertTrue(b >= c);

    test:assertFalse(a < c);
    test:assertFalse(a <= c);
    test:assertTrue(a > c);
    test:assertTrue(a >= c);
}

function testUnionComparison8() {
    string|string:Char a = "A";
    string b = "B";

    test:assertTrue(a < b);
    test:assertTrue(a <= b);
    test:assertFalse(a > b);
    test:assertFalse(a >= b);

    test:assertFalse(b < a);
    test:assertFalse(b <= a);
    test:assertTrue(b > a);
    test:assertTrue(b >= a);
}

function testUnionComparison9() {
    int|int:Signed16 a = 5;
    byte b = 12;

    test:assertTrue(a < b);
    test:assertTrue(a <= b);
    test:assertFalse(a > b);
    test:assertFalse(a >= b);

    test:assertFalse(b < a);
    test:assertFalse(b <= a);
    test:assertTrue(b > a);
    test:assertTrue(b >= a);
}

function testUnionComparison10() {
    decimal|(decimal|decimal) a = 5;
    decimal b = 12;

    test:assertTrue(a < b);
    test:assertTrue(a <= b);
    test:assertFalse(a > b);
    test:assertFalse(a >= b);

    test:assertFalse(b < a);
    test:assertFalse(b <= a);
    test:assertTrue(b > a);
    test:assertTrue(b >= a);
}

function testUnionComparison11() {
    float|(float|float) a = 5.0;
    float b = 12.0;

    test:assertTrue(a < b);
    test:assertTrue(a <= b);
    test:assertFalse(a > b);
    test:assertFalse(a >= b);

    test:assertFalse(b < a);
    test:assertFalse(b <= a);
    test:assertTrue(b > a);
    test:assertTrue(b >= a);
}

function testUnorderedTypeComparison1() {
    [int, string?] a = [59215, "ABC"];
    [int, string?] b = [59215, ()];

    test:assertFalse(a < b);
    test:assertFalse(a <= b);
    test:assertFalse(a > b);
    test:assertFalse(a >= b);
}

function testUnorderedTypeComparison2() {
    [int, float] a = [59215, (0.0/0.0)];
    [int, float] b = [59215, 123.432];

    test:assertFalse(a < b);
    test:assertFalse(a <= b);
    test:assertFalse(a > b);
    test:assertFalse(a >= b);
}

function testUnorderedTypeComparison3() {
    [int, float] a = [59215, (0.0/0.0)];
    [int, float] b = [59215, (0.0/0.0)];

    test:assertFalse(a < b);
    test:assertFalse(a <= b);
    test:assertFalse(a > b);
    test:assertFalse(a >= b);
}

function testUnorderedTypeComparison4() {
    string? a = ();
    string? b = "ABC";

    test:assertFalse(a < b);
    test:assertFalse(a <= b);
    test:assertFalse(a > b);
    test:assertFalse(a >= b);
}

function testUnorderedTypeComparison5() {
    int? a = 400;
    int? b = ();

    test:assertFalse(a < b);
    test:assertFalse(a <= b);
    test:assertFalse(a > b);
    test:assertFalse(a >= b);
}

function testUnorderedTypeComparison6() {
    float a = 400.123;
    float b = (0.0/0.0);

    test:assertFalse(a < b);
    test:assertFalse(a <= b);
    test:assertFalse(a > b);
    test:assertFalse(a >= b);
}

function testUnorderedTypeComparison7() {
    float a = (0.0/0.0);
    float b = (0.0/0.0);

    test:assertFalse(a < b);
    test:assertFalse(a <= b);
    test:assertFalse(a > b);
    test:assertFalse(a >= b);
}

function testUnorderedTypeComparison8() {
    float a = (0.0/0.0);
    float b = 40.34;

    test:assertFalse(a < b);
    test:assertFalse(a <= b);
    test:assertFalse(a > b);
    test:assertFalse(a >= b);
}

function testUnorderedTypeComparison9() {
    decimal? a0 = ();
    decimal b0 = (-32);

    boolean x0 = a0 > b0;
    boolean x1 = b0 > a0;
    test:assertFalse(x0);
    test:assertFalse(x1);

    boolean x2 = a0 >= b0;
    boolean x3 = b0 >= a0;
    test:assertFalse(x2);
    test:assertFalse(x3);

    boolean x4 = a0 < b0;
    boolean x5 = b0 < a0;
    test:assertFalse(x4);
    test:assertFalse(x5);

    boolean x6 = a0 <= b0;
    boolean x7 = b0 <= a0;
    test:assertFalse(x6);
    test:assertFalse(x7);
}

function testUnorderedTypeComparison10() {
    float? a0 = ();
    float b0 = (-32);

    boolean x0 = a0 > b0;
    boolean x1 = b0 > a0;
    test:assertFalse(x0);
    test:assertFalse(x1);

    boolean x2 = a0 >= b0;
    boolean x3 = b0 >= a0;
    test:assertFalse(x2);
    test:assertFalse(x3);

    boolean x4 = a0 < b0;
    boolean x5 = b0 < a0;
    test:assertFalse(x4);
    test:assertFalse(x5);

    boolean x6 = a0 <= b0;
    boolean x7 = b0 <= a0;
    test:assertFalse(x6);
    test:assertFalse(x7);
}

type TwoInts 1|2;
type ThreeInts 3|4|5;

function testUnorderedTypeComparison11() {
    TwoInts a1 = 1;
    TwoInts|ThreeInts a2 = 1;
    byte|int:Signed32 a3 = -1;
    TwoInts? b1 = ();
    TwoInts|null b2 = ();
    TwoInts|ThreeInts|null b3 = ();

    boolean x1 = a1 < b1;
    test:assertFalse(x1);

    boolean x2 = a1 <= b1;
    test:assertFalse(x2);

    boolean x3 = a1 > b1;
    test:assertFalse(x3);

    boolean x4 = a1 >= b1;
    test:assertFalse(x4);

    boolean x5 = a1 < b2;
    test:assertFalse(x5);

    boolean x6 = a1 <= b2;
    test:assertFalse(x6);

    boolean x7 = a1 > b2;
    test:assertFalse(x7);

    boolean x8 = a1 >= b2;
    test:assertFalse(x8);

    boolean x9 = b2 < b1;
    test:assertFalse(x9);

    boolean x10 = b2 <= b1;
    test:assertTrue(x10);

    boolean x11 = b2 > b1;
    test:assertFalse(x11);

    boolean x12 = b2 >= b1;
    test:assertTrue(x12);

    boolean x13 = a2 < b2;
    test:assertFalse(x13);

    boolean x14 = a2 <= b2;
    test:assertFalse(x14);

    boolean x15 = a2 > b2;
    test:assertFalse(x15);

    boolean x16 = a2 >= b2;
    test:assertFalse(x15);

    boolean x17 = a3 < b3;
    boolean x18 = b3 < a3;
    test:assertFalse(x17);
    test:assertFalse(x18);

    boolean x19 = a3 <= b3;
    boolean x20 = b3 <= a3;
    test:assertFalse(x19);
    test:assertFalse(x20);

    boolean x21 = a3 > b3;
    boolean x22 = b3 > a3;
    test:assertFalse(x21);
    test:assertFalse(x22);

    boolean x23 = a2 >= b2;
    boolean x24 = b3 >= a3;
    test:assertFalse(x23);
    test:assertFalse(x24);
}

function testUnorderedTypeComparison12() {
    byte? a0 = ();
    byte b0 = 32;
    byte|null a1 = ();

    boolean x0 = a0 > b0;
    boolean x1 = b0 > a0;
    test:assertFalse(x0);
    test:assertFalse(x1);

    boolean x2 = a0 >= b0;
    boolean x3 = b0 >= a0;
    test:assertFalse(x2);
    test:assertFalse(x3);

    boolean x4 = a0 < b0;
    boolean x5 = b0 < a0;
    test:assertFalse(x4);
    test:assertFalse(x5);

    boolean x6 = a0 <= b0;
    boolean x7 = b0 <= a0;
    test:assertFalse(x6);
    test:assertFalse(x7);

    boolean x8 = a1 > b0;
    boolean x9 = b0 > a1;
    test:assertFalse(x8);
    test:assertFalse(x9);

    boolean x10 = a1 >= b0;
    boolean x11 = b0 >= a1;
    test:assertFalse(x10);
    test:assertFalse(x11);

    boolean x12 = a1 < b0;
    boolean x13 = b0 < a1;
    test:assertFalse(x12);
    test:assertFalse(x13);

    boolean x14 = a1 <= b0;
    boolean x15 = b0 <= a1;
    test:assertFalse(x14);
    test:assertFalse(x15);

    boolean x16 = a1 < a0;
    boolean x17 = a0 <= a1;
    test:assertFalse(x16);
    test:assertTrue(x17);

    boolean x18 = a1 > a0;
    boolean x19 = a0 >= a1;
    test:assertFalse(x18);
    test:assertTrue(x19);
}

function testUnorderedTypeComparison13() {
    int:Signed32? a0 = ();
    int:Signed16 b0 = (-32);

    boolean x0 = a0 > b0;
    boolean x1 = b0 > a0;
    test:assertFalse(x0);
    test:assertFalse(x1);

    boolean x2 = a0 >= b0;
    boolean x3 = b0 >= a0;
    test:assertFalse(x2);
    test:assertFalse(x3);

    boolean x4 = a0 < b0;
    boolean x5 = b0 < a0;
    test:assertFalse(x4);
    test:assertFalse(x5);

    boolean x6 = a0 <= b0;
    boolean x7 = b0 <= a0;
    test:assertFalse(x6);
    test:assertFalse(x7);
}

function testUnorderedTypeComparison14() {
    int:Unsigned16? a0 = ();
    int:Unsigned8 b0 = 32;
    int:Unsigned16|null a1 = ();

    boolean x0 = a0 > b0;
    boolean x1 = b0 > a0;
    test:assertFalse(x0);
    test:assertFalse(x1);

    boolean x2 = a0 >= b0;
    boolean x3 = b0 >= a0;
    test:assertFalse(x2);
    test:assertFalse(x3);

    boolean x4 = a0 < b0;
    boolean x5 = b0 < a0;
    test:assertFalse(x4);
    test:assertFalse(x5);

    boolean x6 = a0 <= b0;
    boolean x7 = b0 <= a0;
    test:assertFalse(x6);
    test:assertFalse(x7);

    boolean x8 = a1 > b0;
    boolean x9 = b0 > a1;
    test:assertFalse(x8);
    test:assertFalse(x9);

    boolean x10 = a1 >= b0;
    boolean x11 = b0 >= a1;
    test:assertFalse(x10);
    test:assertFalse(x11);

    boolean x12 = a1 < b0;
    boolean x13 = b0 < a1;
    test:assertFalse(x12);
    test:assertFalse(x13);

    boolean x14 = a1 <= b0;
    boolean x15 = b0 <= a1;
    test:assertFalse(x14);
    test:assertFalse(x15);

    boolean x16 = a1 < a0;
    boolean x17 = a0 <= a1;
    test:assertFalse(x16);
    test:assertTrue(x17);

    boolean x18 = a1 > a0;
    boolean x19 = a0 >= a1;
    test:assertFalse(x18);
    test:assertTrue(x19);
}

function testUnorderedTypeComparison15() {
    string? a0 = ();
    string b0 = "abc";

    boolean x0 = a0 > b0;
    boolean x1 = b0 > a0;
    test:assertFalse(x0);
    test:assertFalse(x1);

    boolean x2 = a0 >= b0;
    boolean x3 = b0 >= a0;
    test:assertFalse(x2);
    test:assertFalse(x3);

    boolean x4 = a0 < b0;
    boolean x5 = b0 < a0;
    test:assertFalse(x4);
    test:assertFalse(x5);

    boolean x6 = a0 <= b0;
    boolean x7 = b0 <= a0;
    test:assertFalse(x6);
    test:assertFalse(x7);
}

function testUnorderedTypeComparison16() {
    string? a0 = "abc";
    string b0 = "abc";

    boolean x0 = a0 > b0;
    boolean x1 = b0 > a0;
    test:assertFalse(x0);
    test:assertFalse(x1);

    boolean x2 = a0 >= b0;
    boolean x3 = b0 >= a0;
    test:assertTrue(x2);
    test:assertTrue(x3);

    boolean x4 = a0 < b0;
    boolean x5 = b0 < a0;
    test:assertFalse(x4);
    test:assertFalse(x5);

    boolean x6 = a0 <= b0;
    boolean x7 = b0 <= a0;
    test:assertTrue(x6);
    test:assertTrue(x7);
}

function testUnorderedTypeComparison17() {
    string:Char? a0 = ();
    string b0 = "abc";
    string:Char|null a1 = ();

    boolean x0 = a0 > b0;
    boolean x1 = b0 > a0;
    test:assertFalse(x0);
    test:assertFalse(x1);

    boolean x2 = a0 >= b0;
    boolean x3 = b0 >= a0;
    test:assertFalse(x2);
    test:assertFalse(x3);

    boolean x4 = a0 < b0;
    boolean x5 = b0 < a0;
    test:assertFalse(x4);
    test:assertFalse(x5);

    boolean x6 = a0 <= b0;
    boolean x7 = b0 <= a0;
    test:assertFalse(x6);
    test:assertFalse(x7);

    boolean x8 = a1 > b0;
    boolean x9 = b0 > a1;
    test:assertFalse(x8);
    test:assertFalse(x9);

    boolean x10 = a1 >= b0;
    boolean x11 = b0 >= a1;
    test:assertFalse(x10);
    test:assertFalse(x11);

    boolean x12 = a1 < b0;
    boolean x13 = b0 < a1;
    test:assertFalse(x12);
    test:assertFalse(x13);

    boolean x14 = a1 <= b0;
    boolean x15 = b0 <= a1;
    test:assertFalse(x14);
    test:assertFalse(x15);

    boolean x16 = a1 < a0;
    boolean x17 = a0 <= a1;
    test:assertFalse(x16);
    test:assertTrue(x17);

    boolean x18 = a1 > a0;
    boolean x19 = a0 >= a1;
    test:assertFalse(x18);
    test:assertTrue(x19);
}

function testUnorderedTypeComparison18() {
    string:Char? a0 = ();
    string:Char b0 = "a";

    boolean x0 = a0 > b0;
    boolean x1 = b0 > a0;
    test:assertFalse(x0);
    test:assertFalse(x1);

    boolean x2 = a0 >= b0;
    boolean x3 = b0 >= a0;
    test:assertFalse(x2);
    test:assertFalse(x3);

    boolean x4 = a0 < b0;
    boolean x5 = b0 < a0;
    test:assertFalse(x4);
    test:assertFalse(x5);

    boolean x6 = a0 <= b0;
    boolean x7 = b0 <= a0;
    test:assertFalse(x6);
    test:assertFalse(x7);
}

function testUnorderedTypeComparison19() {
    string? a0 = ();
    string:Char b0 = "a";

    boolean x0 = a0 > b0;
    boolean x1 = b0 > a0;
    test:assertFalse(x0);
    test:assertFalse(x1);

    boolean x2 = a0 >= b0;
    boolean x3 = b0 >= a0;
    test:assertFalse(x2);
    test:assertFalse(x3);

    boolean x4 = a0 < b0;
    boolean x5 = b0 < a0;
    test:assertFalse(x4);
    test:assertFalse(x5);

    boolean x6 = a0 <= b0;
    boolean x7 = b0 <= a0;
    test:assertFalse(x6);
    test:assertFalse(x7);
}

type Type1 [boolean?, int, decimal?]; // ordered type -> [boolean?, int, decimal?]
type Type2 [boolean?, int?]; // ordered type -> [boolean?, int?]
// for Type1 and Type2, ordered type can be taken as [boolean?, int?, decimal?...]

function testUnorderedTypeComparison20() {
    Type1 a = [(), 1];
    Type2 b = [()];

    boolean x0 = a > b;
    boolean x1 = b > a;
    test:assertFalse(x0);
    test:assertFalse(x1);

    boolean x2 = a >= b;
    boolean x3 = b >= a;
    test:assertFalse(x2);
    test:assertFalse(x3);

    boolean x4 = a < b;
    boolean x5 = b < a;
    test:assertFalse(x4);
    test:assertFalse(x5);

    boolean x6 = a <= b;
    boolean x7 = b <= a;
    test:assertFalse(x6);
    test:assertFalse(x7);
}

type Type3 [boolean?, int, decimal?]; // ordered type -> [boolean?, int, decimal?]
type Type4 [boolean?, int?]; // ordered type -> [boolean?, int?]
// for Type3 and Type4, ordered type can be taken as [boolean?, int?, decimal?...]

function testUnorderedTypeComparison21() {
    Type3 a = [true, 1];
    Type4 b = [true, 1];

    boolean x0 = a > b;
    boolean x1 = b > a;
    test:assertTrue(x0);
    test:assertFalse(x1);

    boolean x2 = a >= b;
    boolean x3 = b >= a;
    test:assertTrue(x2);
    test:assertFalse(x3);

    boolean x4 = a < b;
    boolean x5 = b < a;
    test:assertFalse(x4);
    test:assertTrue(x5);

    boolean x6 = a <= b;
    boolean x7 = b <= a;
    test:assertFalse(x6);
    test:assertTrue(x7);
}

type Type5 [boolean?, int, byte?, int:Signed32, byte...]; // ordered type -> [boolean?, int, byte?, int:Signed32, byte...]
type Type6 [boolean?, int...]; // ordered type -> [boolean?, int...]
// for Type5 and Type6, ordered type can be taken as [boolean?, int?...]

function testUnorderedTypeComparison22() {
    Type5 a = [(), 1];
    Type6 b = [()];

    boolean x0 = a > b;
    boolean x1 = b > a;
    test:assertTrue(x0);
    test:assertFalse(x1);

    boolean x2 = a >= b;
    boolean x3 = b >= a;
    test:assertTrue(x2);
    test:assertFalse(x3);

    boolean x4 = a < b;
    boolean x5 = b < a;
    test:assertFalse(x4);
    test:assertTrue(x5);

    boolean x6 = a <= b;
    boolean x7 = b <= a;
    test:assertFalse(x6);
    test:assertTrue(x7);
}

type Type7 [boolean?, int, byte?, int:Signed32, byte...] & readonly; // ordered type -> [boolean?, int, byte?, int:Signed32, byte...] & readonly
type Type8 [boolean?, int...]; // ordered type -> [boolean?, int...]
// for Type7 and Type8, ordered type can be taken as [boolean?, int?...]

function testUnorderedTypeComparison23() {
    Type7 a = [(), 1];
    Type8 b = [()];

    boolean x0 = a > b;
    boolean x1 = b > a;
    test:assertTrue(x0);
    test:assertFalse(x1);

    boolean x2 = a >= b;
    boolean x3 = b >= a;
    test:assertTrue(x2);
    test:assertFalse(x3);

    boolean x4 = a < b;
    boolean x5 = b < a;
    test:assertFalse(x4);
    test:assertTrue(x5);

    boolean x6 = a <= b;
    boolean x7 = b <= a;
    test:assertFalse(x6);
    test:assertTrue(x7);
}

type Type9 int?[2]|int[3]; // ordered type -> [int?, int?, int]
type Type10 int?[2]; // ordered type -> [int?, int?]
// for Type9 and Type10, ordered type can be taken as [int?, int?, int...]

function testUnorderedTypeComparison24() {
    Type9 a = [(), 1];
    Type10 b = [()];

    boolean x0 = a > b;
    boolean x1 = b > a;
    test:assertFalse(x0);
    test:assertFalse(x1);

    boolean x2 = a >= b;
    boolean x3 = b >= a;
    test:assertFalse(x2);
    test:assertFalse(x3);

    boolean x4 = a < b;
    boolean x5 = b < a;
    test:assertFalse(x4);
    test:assertFalse(x5);

    boolean x6 = a <= b;
    boolean x7 = b <= a;
    test:assertFalse(x6);
    test:assertFalse(x7);
}

type Type11 byte?[3]|[int, int, byte...]; // ordered type -> [int?, int?, byte?...]
type Type12 int?[2]; // ordered type -> [int?, int?]
// for Type11 and Type12, ordered type can be taken as [int?, int?, byte?...]

function testUnorderedTypeComparison25() {
    Type11 a = [(), 1];
    Type12 b = [()];

    boolean x0 = a > b;
    boolean x1 = b > a;
    test:assertFalse(x0);
    test:assertFalse(x1);

    boolean x2 = a >= b;
    boolean x3 = b >= a;
    test:assertFalse(x2);
    test:assertFalse(x3);

    boolean x4 = a < b;
    boolean x5 = b < a;
    test:assertFalse(x4);
    test:assertFalse(x5);

    boolean x6 = a <= b;
    boolean x7 = b <= a;
    test:assertFalse(x6);
    test:assertFalse(x7);
}

type Int1 1|2;
type Int2 3|4;
type Type13 Int1[3]|Int2[4]; // ordered type -> [int, int, int, int]
type Type14 [int?, byte...] & readonly; // ordered type -> [int?, byte...]
// for Type13 and Type14, ordered type can be taken as [int?, int, int...]

function testUnorderedTypeComparison26() {
    Type13 a = [1, 1, 2];
    Type14 b = [()];

    boolean x0 = a > b;
    boolean x1 = b > a;
    test:assertFalse(x0);
    test:assertFalse(x1);

    boolean x2 = a >= b;
    boolean x3 = b >= a;
    test:assertFalse(x2);
    test:assertFalse(x3);

    boolean x4 = a < b;
    boolean x5 = b < a;
    test:assertFalse(x4);
    test:assertFalse(x5);

    boolean x6 = a <= b;
    boolean x7 = b <= a;
    test:assertFalse(x6);
    test:assertFalse(x7);
}

type Type15 Int1[3]|(Int2[4] & readonly); // ordered type -> [int, int, int, int]
type Type16 [int?, byte...]; // ordered type -> [int?, byte...]
// for Type15 and Type16, ordered type can be taken as [int?, int...]

function testUnorderedTypeComparison27() {
    Type15 a = [1, 1, 2];
    Type16 b = [(), 1, 255, 1];

    boolean x0 = a > b;
    boolean x1 = b > a;
    test:assertFalse(x0);
    test:assertFalse(x1);

    boolean x2 = a >= b;
    boolean x3 = b >= a;
    test:assertFalse(x2);
    test:assertFalse(x3);

    boolean x4 = a < b;
    boolean x5 = b < a;
    test:assertFalse(x4);
    test:assertFalse(x5);

    boolean x6 = a <= b;
    boolean x7 = b <= a;
    test:assertFalse(x6);
    test:assertFalse(x7);
}

function testUnorderedTypeComparison28() {
    byte|int:Signed32 a = -1; // ordered type -> int
    ()|null b = (); // ordered type -> ()
    // for 'byte|int:Signed32' and '()|null', ordered type can be taken as int?

    boolean x0 = a > b;
    boolean x1 = b > a;
    test:assertFalse(x0);
    test:assertFalse(x1);

    boolean x2 = a >= b;
    boolean x3 = b >= a;
    test:assertFalse(x2);
    test:assertFalse(x3);

    boolean x4 = a < b;
    boolean x5 = b < a;
    test:assertFalse(x4);
    test:assertFalse(x5);

    boolean x6 = a <= b;
    boolean x7 = b <= a;
    test:assertFalse(x6);
    test:assertFalse(x7);
}

const float FIVE = 5.0;
const float SIX = 6.0;

type X [()|null, (), (), (), int, (), null]; // ordered type -> [(), (), (), (), int, (), ()]
type Y [FIVE|SIX]; // ordered type -> [int]
// for X and Y, ordered type can be taken as [int?...]

function testUnorderedTypeComparison29() {
    X a = [null];
    Y b = [5];

    boolean x0 = a > b;
    boolean x1 = b > a;
    test:assertFalse(x0);
    test:assertFalse(x1);

    boolean x2 = a >= b;
    boolean x3 = b >= a;
    test:assertFalse(x2);
    test:assertFalse(x3);

    boolean x4 = a < b;
    boolean x5 = b < a;
    test:assertFalse(x4);
    test:assertFalse(x5);

    boolean x6 = a <= b;
    boolean x7 = b <= a;
    test:assertFalse(x6);
    test:assertFalse(x7);
}
