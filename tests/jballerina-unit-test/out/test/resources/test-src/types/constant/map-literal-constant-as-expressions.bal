import ballerina/reflect;

const map<map<boolean>> bm2 = { "key2": bm1 };
const map<boolean> bm1 = { "key1": true };

function testSimpleBooleanConstMap() returns map<boolean> {
    return bm1;
}

function testComplexBooleanConstMap() returns map<map<boolean>> {
    return bm2;
}

// -----------------------------------------------------------

const map<map<int>> im2 = { "key2": im1 };
const map<int> im1 = { "key1": 1 };

function testSimpleIntConstMap() returns map<int> {
    return im1;
}

function testComplexIntConstMap() returns map<map<int>> {
    return im2;
}

// -----------------------------------------------------------

const map<map<byte>> bytem2 = { "key2": bytem1 };
const map<byte> bytem1 = { "key1": 10 };

function testSimpleByteConstMap() returns map<byte> {
    return bytem1;
}

function testComplexByteConstMap() returns map<map<byte>> {
    return bytem2;
}

// -----------------------------------------------------------

const map<map<float>> fm2 = { "key2": fm1 };
const map<float> fm1 = { "key1": 2.0 };

function testSimpleFloatConstMap() returns map<float> {
    return fm1;
}

function testComplexFloatConstMap() returns map<map<float>> {
    return fm2;
}

// -----------------------------------------------------------

const map<map<decimal>> dm2 = { "key2": dm1 };
const map<decimal> dm1 = { "key1": 100 };

function testSimpleDecimalConstMap() returns map<decimal> {
    return dm1;
}

function testComplexDecimalConstMap() returns map<map<decimal>> {
    return dm2;
}

// -----------------------------------------------------------

const map<map<string>> sm2 = { "key2": sm1 };
const map<string> sm1 = { "key1": "value1" };

function testSimpleStringConstMap() returns map<string> {
    return sm1;
}

function testComplexStringConstMap() returns map<map<string>> {
    return sm2;
}

// -----------------------------------------------------------

const map<map<map<string>>> m3 = { "k3": m2 };

const map<map<string>> m2 = { "k2": m1 };

const map<string> m1 = { "k1": sVal };

const sVal = "v1";

function testComplexConstMap() returns map<map<map<string>>> {
    return m3;
}

// -----------------------------------------------------------

const sConst = "Ballerina";
const iConst = 100;
const map<string> mConst = { "mKey": "mValue" };

public type TestConfig record {|
    string s;
    int i;
    map<string> m;
|};

public annotation<function> testAnnotation TestConfig;


@testAnnotation {
    s: sConst,
    i: iConst,
    m: mConst
}
function testFunction() {

}

function testConstInAnnotations() returns reflect:annotationData[] {
    return reflect:getFunctionAnnotations(testFunction);
}

// -----------------------------------------------------------

map<map<string>> m4 = { "m4k": m5 };
const map<string> m5 = { "m5k": "m5v" };

// Negative tests.
function getNestedConstantMapValue() returns string {
    return m4["m4k"]["m5k"];
}

function updateNestedConstantMapValue() {
    m4["m4k"]["m5k"] = "m5nv";
}

function updateNestedConstantMapValue2() {
    m4["m4k"]["newKey"] = "newValue";
}

// -----------------------------------------------------------

map<string>[] a1 = [m5];

// Negative tests.
function updateConstantMapValueInArray() {
    a1[0]["m5k"] = "m5nv";
}

function updateConstantMapValueInArray2() {
    a1[0]["newKey"] = "newValue";
}

function getConstantMapValueInArray() returns string {
    return a1[0]["m5k"];
}

// -----------------------------------------------------------

// Negative tests.
function updateReturnedConstantMap() {
    map<string> m = getMap();
    m.m5k = "m5kn";
}

function updateReturnedConstantMap2() {
    map<string> m = getMap();
    m["newKey"] = "newValue";
}

function getMap() returns map<string> {
    return m5;
}

// -----------------------------------------------------------

const map<boolean> bm3 = { "bm3k": true };
const map<boolean> bm4 = { "bm4kn": bm3["bm3k"] };

function testBooleanConstKeyReference() returns map<boolean> {
    return bm4;
}

// -----------------------------------------------------------

const map<int> im3 = { "im3k": 123 };
const map<int> im4 = { "im4kn": im3.im3k };

function testIntConstKeyReference() returns map<int> {
    return im4;
}

// -----------------------------------------------------------

const map<byte> bytem3 = { "bytem3k": 64 };
const map<byte> bytem4 = { "bytem4kn": bytem3.bytem3k };

function testByteConstKeyReference() returns map<byte> {
    return bytem4;
}

// -----------------------------------------------------------

const map<float> fm3 = { "fm3k": 12.5 };
const map<float> fm4 = { "fm4kn": fm3["fm3k"] };

function testFloatConstKeyReference() returns map<float> {
    return fm4;
}

// -----------------------------------------------------------

const map<decimal> dm3 = { "dm3k": 5.56 };
const map<decimal> dm4 = { "dm4kn": dm3["dm3k"] };

function testDecimalConstKeyReference() returns map<decimal> {
    return dm4;
}

// -----------------------------------------------------------

const map<string> sm3 = { "sm3k": "sm3v" };
const map<string> sm4 = { "sm4kn": sm3["sm3k"] };

function testStringConstKeyReference() returns map<string> {
    return sm4;
}

// -----------------------------------------------------------

const map<()> nm3 = { "nm3k": () };
const map<()> nm4 = { "nm4kn": nm3["nm3k"] };

function testNullConstKeyReference() returns map<()> {
    return nm4;
}

// -----------------------------------------------------------

function testBooleanConstKeyReferenceInLocalVar() returns boolean {
    boolean b = bm3["bm3k"];
    return b;
}

// -----------------------------------------------------------

function testIntConstKeyReferenceInLocalVar() returns int {
    int i = im3["im3k"];
    return i;
}

// -----------------------------------------------------------

function testByteConstKeyReferenceInLocalVar() returns byte {
    byte b = bytem3["bytem3k"];
    return b;
}

// -----------------------------------------------------------

function testFloatConstKeyReferenceInLocalVar() returns float {
    float f = fm3["fm3k"];
    return f;
}

// -----------------------------------------------------------

function testDecimalConstKeyReferenceInLocalVar() returns decimal {
    decimal d = dm3["dm3k"];
    return d;
}

// -----------------------------------------------------------

function testStringConstKeyReferenceInLocalVar() returns string {
    string s = sm3["sm3k"];
    return s;
}

// -----------------------------------------------------------

function testNullConstKeyReferenceInLocalVar() returns () {
    () n = nm3["nm3k"];
    return n;
}
