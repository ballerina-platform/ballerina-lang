import testorg/foo version v1;

import ballerina/reflect;

function testSimpleBooleanConstMap() returns map<boolean> {
    return foo:bm1;
}

function testComplexBooleanConstMap() returns map<map<boolean>> {
    return foo:bm2;
}

// -----------------------------------------------------------

function testSimpleIntConstMap() returns map<int> {
    return foo:im1;
}

function testComplexIntConstMap() returns map<map<int>> {
    return foo:im2;
}

// -----------------------------------------------------------

function testSimpleByteConstMap() returns map<byte> {
    return foo:bytem1;
}

function testComplexByteConstMap() returns map<map<byte>> {
    return foo:bytem2;
}

// -----------------------------------------------------------

function testSimpleFloatConstMap() returns map<float> {
    return foo:fm1;
}

function testComplexFloatConstMap() returns map<map<float>> {
    return foo:fm2;
}

// -----------------------------------------------------------

function testSimpleDecimalConstMap() returns map<decimal> {
    return foo:dm1;
}

function testComplexDecimalConstMap() returns map<map<decimal>> {
    return foo:dm2;
}

// -----------------------------------------------------------

function testSimpleStringConstMap() returns map<string> {
    return foo:sm1;
}

function testComplexStringConstMap() returns map<map<string>> {
    return foo:sm2;
}

// -----------------------------------------------------------

function testSimpleNilConstMap() returns map<()> {
    return foo:nm1;
}

function testComplexNilConstMap() returns map<map<()>> {
    return foo:nm2;
}

// -----------------------------------------------------------

function testComplexConstMap() returns map<map<map<string>>> {
    return foo:m3;
}

// -----------------------------------------------------------

@foo:testAnnotation {
    s: foo:sConst,
    i: foo:iConst,
    m: foo:mConst
}
function testFunction() {

}

function testConstInAnnotations() returns reflect:annotationData[] {
    return reflect:getFunctionAnnotations(testFunction);
}

// -----------------------------------------------------------

function getNestedConstantMapValue() returns string {
    return foo:m4.m4k.m5k;
}

// Negative tests.
function updateNestedConstantMapValue() {
    foo:m4.m4k.m5k = "m5nv";
}

function updateNestedConstantMapValue2() {
    foo:m4.m4k.newKey = "newValue";
}

// -----------------------------------------------------------

// Negative tests.
function updateConstantMapValueInArray() {
    foo:a1[0].m5k = "m5nv";
}

function updateConstantMapValueInArray2() {
    foo:a1[0].newKey = "newValue";
}

function getConstantMapValueInArray() returns string {
    return foo:a1[0].m5k;
}

// -----------------------------------------------------------

// Negative tests.
function updateReturnedConstantMap() {
    map<string> m = getMap();
    m.m5k = "m5kn";
}

function updateReturnedConstantMap2() {
    map<string> m = getMap();
    m.newKey = "newValue";
}

function getMap() returns map<string> {
    return foo:m5;
}

// -----------------------------------------------------------

function testBooleanConstKeyReference() returns map<boolean> {
    return foo:bm4;
}

// -----------------------------------------------------------

function testIntConstKeyReference() returns map<int> {
    return foo:im4;
}

// -----------------------------------------------------------

function testByteConstKeyReference() returns map<byte> {
    return foo:bytem4;
}

// -----------------------------------------------------------

function testFloatConstKeyReference() returns map<float> {
    return foo:fm4;
}

// -----------------------------------------------------------

function testDecimalConstKeyReference() returns map<decimal> {
    return foo:dm4;
}

// -----------------------------------------------------------

function testStringConstKeyReference() returns map<string> {
    return foo:sm4;
}

// -----------------------------------------------------------

function testNullConstKeyReference() returns map<()> {
    return foo:nm4;
}

// -----------------------------------------------------------

function testBooleanConstKeyReferenceInLocalVar() returns boolean {
    boolean b = foo:bm3.bm3k;
    return b;
}

// -----------------------------------------------------------

function testIntConstKeyReferenceInLocalVar() returns int {
    int i = foo:im3.im3k;
    return i;
}

// -----------------------------------------------------------

function testByteConstKeyReferenceInLocalVar() returns byte {
    byte b = foo:bytem3.bytem3k;
    return b;
}

// -----------------------------------------------------------

function testFloatConstKeyReferenceInLocalVar() returns float {
    float f = foo:fm3.fm3k;
    return f;
}

// -----------------------------------------------------------

function testDecimalConstKeyReferenceInLocalVar() returns decimal {
    decimal d = foo:dm3.dm3k;
    return d;
}

// -----------------------------------------------------------

function testStringConstKeyReferenceInLocalVar() returns string {
    string s = foo:sm3.sm3k;
    return s;
}

// -----------------------------------------------------------

function testNullConstKeyReferenceInLocalVar() returns () {
    () n = foo:nm3.nm3k;
    return n;
}
