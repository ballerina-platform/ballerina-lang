import ballerina.reflect;

// Start Nulls


function testNullTruePositive() (boolean) {
    string s1 = null;
    string s2 = null;
    return reflect:equals(s1,s2);
}

function testNullTrueNegative() (boolean) {
    string s1 = null;
    string s2 = "b";
    return reflect:equals(s1,s2);
}

function testNullFalseNegative() (boolean) {
    string s1 = null;
    string s2 = null;
    return !reflect:equals(s1,s2);
}

function testNullFalsePositive() (boolean) {
    string s1 = null;
    string s2 = "b";
    return !reflect:equals(s1,s2);
}

// End Nulls

// Start Strings

function testStringTruePositive() (boolean) {
    string s1 = "a";
    string s2 = "a";
    return reflect:equals(s1,s2);
}

function testStringTrueNegative() (boolean) {
    string s1 = "a";
    string s2 = "b";
    return reflect:equals(s1,s2);
}

function testStringFalseNegative() (boolean) {
    string s1 = "a";
    string s2 = "a";
    return !reflect:equals(s1,s2);
}

function testStringFalsePositive() (boolean) {
    string s1 = "a";
    string s2 = "b";
    return !reflect:equals(s1,s2);
}

// End Strings

// Start Int

function testIntTruePositive() (boolean) {
    int i1 = 5;
    int i2 = 5;
    return reflect:equals(i1,i2);
}

function testIntTrueNegative() (boolean) {
    int i1 = 5;
    int i2 = 10;
    return reflect:equals(i1,i2);
}

function testIntFalseNegative() (boolean) {
    int i1 = 5;
    int i2 = 5;
    return !reflect:equals(i1,i2);
}

function testIntFalsePositive() (boolean) {
    int i1 = 5;
    int i2 = 10;
    return !reflect:equals(i1,i2);
}

// End Int

// Start Float

function testFloatTruePositive() (boolean) {
    float f1 = 2.0;
    float f2 = 2.0;
    return reflect:equals(f1,f2);
}

function testFloatTrueNegative() (boolean) {
    float f1 = 2.0;
    float f2 = 5.0;
    return reflect:equals(f1,f2);
}

function testFloatFalseNegative() (boolean) {
    float f1 = 2.0;
    float f2 = 2.0;
    return !reflect:equals(f1,f2);
}

function testFloatFalsePositive() (boolean) {
    float f1 = 2.0;
    float f2 = 5.0;
    return !reflect:equals(f1,f2);
}

// End Float

// Start Boolean

function testBooleanTruePositive() (boolean) {
    boolean b1 = true;
    boolean b2 = true;
    return reflect:equals(b1,b2);
}

function testBooleanTrueNegative() (boolean) {
    boolean b1 = true;
    boolean b2 = false;
    return reflect:equals(b1,b2);
}

function testBooleanFalseNegative() (boolean) {
    boolean b1 = true;
    boolean b2 = true;
    return !reflect:equals(b1,b2);
}

function testBooleanFalsePositive() (boolean) {
    boolean b1 = true;
    boolean b2 = false;
    return !reflect:equals(b1,b2);
}

// End Boolean

// Start Type

function testTypeTruePositive() (boolean) {
    type t1 = (typeof int);
    type t2 = (typeof int);
    return reflect:equals(t1,t2);
}

function testTypeTrueNegative() (boolean) {
    type t1 = (typeof int);
    type t2 = (typeof string);
    return reflect:equals(t1,t2);
}

function testTypeFalseNegative() (boolean) {
    type t1 = (typeof int);
    type t2 = (typeof int);
    return !reflect:equals(t1,t2);
}

function testTypeFalsePositive() (boolean) {
    type t1 = (typeof int);
    type t2 = (typeof string);
    return !reflect:equals(t1,t2);
}

// End Type

// Start String Arrays

function testStringArrayTruePositive() (boolean) {
    string[] s1 = ["a", "b"];
    string[] s2 = ["a", "b"];
    return reflect:equals(s1,s2);
}

function testStringArrayTrueNegative() (boolean) {
    string[] s1 = ["a", "b"];
    string[] s2 = ["a", "c"];
    return reflect:equals(s1,s2);
}

function testStringArrayFalseNegative() (boolean) {
    string[] s1 = ["a", "b"];
    string[] s2 = ["a", "b"];
    return !reflect:equals(s1,s2);
}

function testStringArrayFalsePositive() (boolean) {
    string[] s1 = ["a", "b"];
    string[] s2 = ["a", "c"];
    return !reflect:equals(s1,s2);
}

// End String Arrays

// Start Int Arrays

function testIntArrayTruePositive() (boolean) {
    int[] i1 = [5, 10];
    int[] i2 = [5, 10];
    return reflect:equals(i1,i2);
}

function testIntArrayTrueNegative() (boolean) {
    int[] i1 = [5, 10];
    int[] i2 = [10, 10];
    return reflect:equals(i1,i2);
}

function testIntArrayFalseNegative() (boolean) {
    int[] i1 = [5, 10];
    int[] i2 = [5, 10];
    return !reflect:equals(i1,i2);
}

function testIntArrayFalsePositive() (boolean) {
    int[] i1 = [5, 10];
    int[] i2 = [10, 10];
    return !reflect:equals(i1,i2);
}

// End Int Arrays

// Start Float Arrays

function testFloatArrayTruePositive() (boolean) {
    float[] f1 = [2.0, 20.5];
    float[] f2 = [2.0, 20.5];
    return reflect:equals(f1,f2);
}

function testFloatArrayTrueNegative() (boolean) {
    float[] f1 = [2.0, 20.5];
    float[] f2 = [7.5, 20.5];
    return reflect:equals(f1,f2);
}

function testFloatArrayFalseNegative() (boolean) {
    float[] f1 = [2.0, 20.5];
    float[] f2 = [2.0, 20.5];
    return !reflect:equals(f1,f2);
}

function testFloatArrayFalsePositive() (boolean) {
    float[] f1 = [2.0, 20.5];
    float[] f2 = [7.5, 20.5];
    return !reflect:equals(f1,f2);
}

// End Float Arrays

// Start Boolean Arrays

function testBooleanArrayTruePositive() (boolean) {
    boolean[] b1 = [true, true];
    boolean[] b2 = [true, true];
    return reflect:equals(b1,b2);
}

function testBooleanArrayTrueNegative() (boolean) {
    boolean[] b1 = [true, true];
    boolean[] b2 = [true, false];
    return reflect:equals(b1,b2);
}

function testBooleanArrayFalseNegative() (boolean) {
    boolean[] b1 = [true, true];
    boolean[] b2 = [true, true];
    return !reflect:equals(b1,b2);
}

function testBooleanArrayFalsePositive() (boolean) {
    boolean[] b1 = [true, true];
    boolean[] b2 = [true, false];
    return !reflect:equals(b1,b2);
}

// End Boolean Arrays

// Start Type Arrays

function testTypeArrayTruePositive() (boolean) {
    type[] t1 = [(typeof int), (typeof string)];
    type[] t2 = [(typeof int), (typeof string)];
    return reflect:equals(t1,t2);
}

function testTypeArrayTrueNegative() (boolean) {
    type[] t1 = [(typeof int), (typeof string)];
    type[] t2 = [(typeof int), (typeof map)];
    return reflect:equals(t1,t2);
}

function testTypeArrayFalseNegative() (boolean) {
    type[] t1 = [(typeof int), (typeof string)];
    type[] t2 = [(typeof int), (typeof string)];
    return !reflect:equals(t1,t2);
}

function testTypeArrayFalsePositive() (boolean) {
    type[] t1 = [(typeof int), (typeof string)];
    type[] t2 = [(typeof int), (typeof map)];
    return !reflect:equals(t1,t2);
}

// End Type Arrays