import testorg/foo version v1;

import ballerina/reflect;

function testSimpleBooleanConstMap() returns map<boolean> {
    return foo:bm1;
}

function testComplexBooleanConstMap() returns map<map<boolean>> {
    return foo:bm3;
}

// -----------------------------------------------------------

function testSimpleIntConstMap() returns map<int> {
    return foo:im1;
}

function testComplexIntConstMap() returns map<map<int>> {
    return foo:im3;
}

// -----------------------------------------------------------

function testSimpleByteConstMap() returns map<byte> {
    return foo:bytem1;
}

function testComplexByteConstMap() returns map<map<byte>> {
    return foo:bytem3;
}

// -----------------------------------------------------------

function testSimpleFloatConstMap() returns map<float> {
    return foo:fm1;
}

function testComplexFloatConstMap() returns map<map<float>> {
    return foo:fm3;
}

// -----------------------------------------------------------

function testSimpleDecimalConstMap() returns map<decimal> {
    return foo:dm1;
}

function testComplexDecimalConstMap() returns map<map<decimal>> {
    return foo:dm3;
}

// -----------------------------------------------------------

function testSimpleStringConstMap() returns map<string> {
    return foo:sm1;
}

function testComplexStringConstMap() returns map<map<string>> {
    return foo:sm3;
}

// -----------------------------------------------------------

function testSimpleNilConstMap() returns map<()> {
    return foo:nm1;
}

function testComplexNilConstMap() returns map<map<()>> {
    return foo:nm3;
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
    return foo:m5.m5k.m6k;
}

// Negative tests.
function updateNestedConstantMapValue() {
    foo:m5.m5k.m6k = "m6nv";
}

function updateNestedConstantMapValue2() {
    foo:m5.m5k.newKey = "newValue";
}

// -----------------------------------------------------------

// Negative tests.
function updateConstantMapValueInArray() {
    foo:a1[0].m6k = "m6nv";
}

function updateConstantMapValueInArray2() {
    foo:a1[0].newKey = "newValue";
}

function getConstantMapValueInArray() returns string {
    return foo:a1[0].m6k;
}

// -----------------------------------------------------------

// Negative tests.
function updateReturnedConstantMap() {
    map<string> m = getMap();
    m.m6k = "m6kn";
}

function updateReturnedConstantMap2() {
    map<string> m = getMap();
    m.newKey = "newValue";
}

function getMap() returns map<string> {
    return foo:m6;
}

// -----------------------------------------------------------

function testBooleanConstKeyReference() returns map<boolean> {
    return foo:bm5;
}

// -----------------------------------------------------------

function testIntConstKeyReference() returns map<int> {
    return foo:im5;
}

// -----------------------------------------------------------

function testByteConstKeyReference() returns map<byte> {
    return foo:bytem5;
}

// -----------------------------------------------------------

function testFloatConstKeyReference() returns map<float> {
    return foo:fm5;
}

// -----------------------------------------------------------

function testDecimalConstKeyReference() returns map<decimal> {
    return foo:dm5;
}

// -----------------------------------------------------------

function testStringConstKeyReference() returns map<string> {
    return foo:sm5;
}

// -----------------------------------------------------------

function testNullConstKeyReference() returns map<()> {
    return foo:nm5;
}

// -----------------------------------------------------------

function testBooleanConstKeyReferenceInLocalVar() returns boolean {
    boolean b = foo:bm4.bm4k;
    return b;
}

// -----------------------------------------------------------

function testIntConstKeyReferenceInLocalVar() returns int {
    int i = foo:im4.im4k;
    return i;
}

// -----------------------------------------------------------

function testByteConstKeyReferenceInLocalVar() returns byte {
    byte b = foo:bytem4.bytem4k;
    return b;
}

// -----------------------------------------------------------

function testFloatConstKeyReferenceInLocalVar() returns float {
    float f = foo:fm4.fm4k;
    return f;
}

// -----------------------------------------------------------

function testDecimalConstKeyReferenceInLocalVar() returns decimal {
    decimal d = foo:dm4.dm4k;
    return d;
}

// -----------------------------------------------------------

function testStringConstKeyReferenceInLocalVar() returns string {
    string s = foo:sm4.sm4k;
    return s;
}

// -----------------------------------------------------------

function testNullConstKeyReferenceInLocalVar() returns () {
    () n = foo:nm4.nm4k;
    return n;
}
