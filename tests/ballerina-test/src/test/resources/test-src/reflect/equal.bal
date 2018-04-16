import ballerina/reflect;

// Start Nulls


function testNullTruePositive() returns (boolean) {
    string | () s1;
    string | () s2;
    return reflect:equals(s1,s2);
}

function testNullTrueNegative() returns (boolean) {
    string |() s1;
    string s2 = "b";
    return reflect:equals(s1,s2);
}

function testNullFalseNegative() returns (boolean) {
    string | () s1;
    string | () s2;
    return !reflect:equals(s1,s2);
}

function testNullFalsePositive() returns (boolean) {
    string | () s1;
    string s2 = "b";
    return !reflect:equals(s1,s2);
}

// End Nulls

// Start Strings

function testStringTruePositive() returns (boolean) {
    string s1 = "a";
    string s2 = "a";
    return reflect:equals(s1,s2);
}

function testStringTrueNegative() returns (boolean) {
    string s1 = "a";
    string s2 = "b";
    return reflect:equals(s1,s2);
}

function testStringFalseNegative() returns (boolean) {
    string s1 = "a";
    string s2 = "a";
    return !reflect:equals(s1,s2);
}

function testStringFalsePositive() returns (boolean) {
    string s1 = "a";
    string s2 = "b";
    return !reflect:equals(s1,s2);
}

// End Strings

// Start Int

function testIntTruePositive() returns (boolean) {
    int i1 = 5;
    int i2 = 5;
    return reflect:equals(i1,i2);
}

function testIntTrueNegative() returns (boolean) {
    int i1 = 5;
    int i2 = 10;
    return reflect:equals(i1,i2);
}

function testIntFalseNegative() returns (boolean) {
    int i1 = 5;
    int i2 = 5;
    return !reflect:equals(i1,i2);
}

function testIntFalsePositive() returns (boolean) {
    int i1 = 5;
    int i2 = 10;
    return !reflect:equals(i1,i2);
}

// End Int

// Start Float

function testFloatTruePositive() returns (boolean) {
    float f1 = 2.0;
    float f2 = 2.0;
    return reflect:equals(f1,f2);
}

function testFloatTrueNegative() returns (boolean) {
    float f1 = 2.0;
    float f2 = 5.0;
    return reflect:equals(f1,f2);
}

function testFloatFalseNegative() returns (boolean) {
    float f1 = 2.0;
    float f2 = 2.0;
    return !reflect:equals(f1,f2);
}

function testFloatFalsePositive() returns (boolean) {
    float f1 = 2.0;
    float f2 = 5.0;
    return !reflect:equals(f1,f2);
}

// End Float

// Start Boolean

function testBooleanTruePositive() returns (boolean) {
    boolean b1 = true;
    boolean b2 = true;
    return reflect:equals(b1,b2);
}

function testBooleanTrueNegative() returns (boolean) {
    boolean b1 = true;
    boolean b2 = false;
    return reflect:equals(b1,b2);
}

function testBooleanFalseNegative() returns (boolean) {
    boolean b1 = true;
    boolean b2 = true;
    return !reflect:equals(b1,b2);
}

function testBooleanFalsePositive() returns (boolean) {
    boolean b1 = true;
    boolean b2 = false;
    return !reflect:equals(b1,b2);
}

// End Boolean

// Start Type

function testTypeTruePositive() returns (boolean) {
    typedesc t1 = (int);
    typedesc t2 = (int);
    return reflect:equals(t1,t2);
}

function testTypeTrueNegative() returns (boolean) {
    typedesc t1 = (int);
    typedesc t2 = (string);
    return reflect:equals(t1,t2);
}

function testTypeFalseNegative() returns (boolean) {
    typedesc t1 = (int);
    typedesc t2 = (int);
    return !reflect:equals(t1,t2);
}

function testTypeFalsePositive() returns (boolean) {
    typedesc t1 = (int);
    typedesc t2 = (string);
    return !reflect:equals(t1,t2);
}

// End Type

// Start String Arrays

function testStringArrayTruePositive() returns (boolean) {
    string[] s1 = ["a", "b"];
    string[] s2 = ["a", "b"];
    return reflect:equals(s1,s2);
}

function testStringArrayTrueNegative() returns (boolean) {
    string[] s1 = ["a", "b"];
    string[] s2 = ["a", "c"];
    return reflect:equals(s1,s2);
}

function testStringArrayFalseNegative() returns (boolean) {
    string[] s1 = ["a", "b"];
    string[] s2 = ["a", "b"];
    return !reflect:equals(s1,s2);
}

function testStringArrayFalsePositive() returns (boolean) {
    string[] s1 = ["a", "b"];
    string[] s2 = ["a", "c"];
    return !reflect:equals(s1,s2);
}

// End String Arrays

// Start Int Arrays

function testIntArrayTruePositive() returns (boolean) {
    int[] i1 = [5, 10];
    int[] i2 = [5, 10];
    return reflect:equals(i1,i2);
}

function testIntArrayTrueNegative() returns (boolean) {
    int[] i1 = [5, 10];
    int[] i2 = [10, 10];
    return reflect:equals(i1,i2);
}

function testIntArrayFalseNegative() returns (boolean) {
    int[] i1 = [5, 10];
    int[] i2 = [5, 10];
    return !reflect:equals(i1,i2);
}

function testIntArrayFalsePositive() returns (boolean) {
    int[] i1 = [5, 10];
    int[] i2 = [10, 10];
    return !reflect:equals(i1,i2);
}

// End Int Arrays

// Start Float Arrays

function testFloatArrayTruePositive() returns (boolean) {
    float[] f1 = [2.0, 20.5];
    float[] f2 = [2.0, 20.5];
    return reflect:equals(f1,f2);
}

function testFloatArrayTrueNegative() returns (boolean) {
    float[] f1 = [2.0, 20.5];
    float[] f2 = [7.5, 20.5];
    return reflect:equals(f1,f2);
}

function testFloatArrayFalseNegative() returns (boolean) {
    float[] f1 = [2.0, 20.5];
    float[] f2 = [2.0, 20.5];
    return !reflect:equals(f1,f2);
}

function testFloatArrayFalsePositive() returns (boolean) {
    float[] f1 = [2.0, 20.5];
    float[] f2 = [7.5, 20.5];
    return !reflect:equals(f1,f2);
}

// End Float Arrays

// Start Boolean Arrays

function testBooleanArrayTruePositive() returns (boolean) {
    boolean[] b1 = [true, true];
    boolean[] b2 = [true, true];
    return reflect:equals(b1,b2);
}

function testBooleanArrayTrueNegative() returns (boolean) {
    boolean[] b1 = [true, true];
    boolean[] b2 = [true, false];
    return reflect:equals(b1,b2);
}

function testBooleanArrayFalseNegative() returns (boolean) {
    boolean[] b1 = [true, true];
    boolean[] b2 = [true, true];
    return !reflect:equals(b1,b2);
}

function testBooleanArrayFalsePositive() returns (boolean) {
    boolean[] b1 = [true, true];
    boolean[] b2 = [true, false];
    return !reflect:equals(b1,b2);
}

// End Boolean Arrays

// Start Type Arrays

function testTypeArrayTruePositive() returns (boolean) {
    typedesc[] t1 = [(int), (string)];
    typedesc[] t2 = [(int), (string)];
    return reflect:equals(t1,t2);
}

function testTypeArrayTrueNegative() returns (boolean) {
    typedesc[] t1 = [(int), (string)];
    typedesc[] t2 = [(int), (map)];
    return reflect:equals(t1,t2);
}

function testTypeArrayFalseNegative() returns (boolean) {
    typedesc[] t1 = [(int), (string)];
    typedesc[] t2 = [(int), (string)];
    return !reflect:equals(t1,t2);
}

function testTypeArrayFalsePositive() returns (boolean) {
    typedesc[] t1 = [(int), (string)];
    typedesc[] t2 = [(int), (map)];
    return !reflect:equals(t1,t2);
}

// End Type Arrays