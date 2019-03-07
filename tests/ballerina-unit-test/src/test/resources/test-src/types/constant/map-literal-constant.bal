import ballerina/reflect;

const map<map<boolean>> bm3 = { "key3": bm1, "key4": bm2 };
const map<boolean> bm1 = { "key1": true };
const map<boolean> bm2 = { "key2": false };

function testSimpleBooleanConstMap() returns map<boolean> {
    return bm1;
}

function testComplexBooleanConstMap() returns map<map<boolean>> {
    return bm3;
}

// -----------------------------------------------------------

const map<map<int>> im3 = { "key3": im1, "key4": im2 };
const map<int> im1 = { "key1": 1 };
const map<int> im2 = { "key2": 2 };

function testSimpleIntConstMap() returns map<int> {
    return im1;
}

function testComplexIntConstMap() returns map<map<int>> {
    return im3;
}

// -----------------------------------------------------------

const map<map<byte>> bytem3 = { "key3": bytem1, "key4": bytem2 };
const map<byte> bytem1 = { "key1": 10 };
const map<byte> bytem2 = { "key2": 20 };

function testSimpleByteConstMap() returns map<byte> {
    return bytem1;
}

function testComplexByteConstMap() returns map<map<byte>> {
    return bytem3;
}

// -----------------------------------------------------------

const map<map<float>> fm3 = { "key3": fm1, "key4": fm2 };
const map<float> fm1 = { "key1": 2.0 };
const map<float> fm2 = { "key2": 4.0 };

function testSimpleFloatConstMap() returns map<float> {
    return fm1;
}

function testComplexFloatConstMap() returns map<map<float>> {
    return fm3;
}

// -----------------------------------------------------------

const map<map<decimal>> dm3 = { "key3": dm1, "key4": dm2 };
const map<decimal> dm1 = { "key1": 100 };
const map<decimal> dm2 = { "key2": 200 };

function testSimpleDecimalConstMap() returns map<decimal> {
    return dm1;
}

function testComplexDecimalConstMap() returns map<map<decimal>> {
    return dm3;
}

// -----------------------------------------------------------

const map<map<string>> sm3 = { "key3": sm1, "key4": sm2 };
const map<string> sm1 = { "key1": "value1" };
const map<string> sm2 = { "key2": "value2" };

function testSimpleStringConstMap() returns map<string> {
    return sm1;
}

function testComplexStringConstMap() returns map<map<string>> {
    return sm3;
}

// -----------------------------------------------------------

const map<map<()>> nm3 = { "key3": nm1, "key4": nm2 };
const map<()> nm1 = { "key1": () };
const map<()> nm2 = { "key2": () };

function testSimpleNilConstMap() returns map<()> {
    return nm1;
}

function testComplexNilConstMap() returns map<map<()>> {
    return nm3;
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

const map<boolean> bm4 = { "bm4k": true };
const map<boolean> bm5 = { "bm5kn": bm4.bm4k };

function testBooleanConstKeyReference() returns map<boolean> {
    return bm5;
}

// -----------------------------------------------------------

const map<int> im4 = { "im4k": 123 };
const map<int> im5 = { "im5kn": im4.im4k };

function testIntConstKeyReference() returns map<int> {
    return im5;
}

// -----------------------------------------------------------

const map<byte> bytem4 = { "bytem4k": 64 };
const map<byte> bytem5 = { "bytem5kn": bytem4.bytem4k };

function testByteConstKeyReference() returns map<byte> {
    return bytem5;
}

// -----------------------------------------------------------

const map<float> fm4 = { "fm4k": 12.5 };
const map<float> fm5 = { "fm5kn": fm4.fm4k };

function testFloatConstKeyReference() returns map<float> {
    return fm5;
}

// -----------------------------------------------------------

const map<decimal> dm4 = { "dm4k": 5.56 };
const map<decimal> dm5 = { "dm5kn": dm4.dm4k };

function testDecimalConstKeyReference() returns map<decimal> {
    return dm5;
}

// -----------------------------------------------------------

const map<string> sm4 = { "sm4k": "sm4v" };
const map<string> sm5 = { "sm5kn": sm4.sm4k };

function testStringConstKeyReference() returns map<string> {
    return sm5;
}

// -----------------------------------------------------------

const map<()> nm4 = { "nm4k": () };
const map<()> nm5 = { "nm5kn": nm4.nm4k };

function testNullConstKeyReference() returns map<()> {
    return nm5;
}

// -----------------------------------------------------------

function testBooleanConstKeyReferenceInLocalVar() returns boolean {
    boolean b = bm4.bm4k;
    return b;
}

// -----------------------------------------------------------

function testIntConstKeyReferenceInLocalVar() returns int {
    int i = im4.im4k;
    return i;
}

// -----------------------------------------------------------

function testByteConstKeyReferenceInLocalVar() returns byte {
    byte b = bytem4.bytem4k;
    return b;
}

// -----------------------------------------------------------

function testFloatConstKeyReferenceInLocalVar() returns float {
    float f = fm4.fm4k;
    return f;
}

// -----------------------------------------------------------

function testDecimalConstKeyReferenceInLocalVar() returns decimal {
    decimal d = dm4.dm4k;
    return d;
}

// -----------------------------------------------------------

function testStringConstKeyReferenceInLocalVar() returns string {
    string s = sm4.sm4k;
    return s;
}

// -----------------------------------------------------------

function testNullConstKeyReferenceInLocalVar() returns () {
    () n = nm4.nm4k;
    return n;
}

// annotations -----------------------------------------------

const sConst = "Ballerina";
const iConst = 100;
const map<string> mConst = { "mKey": "mValue" };

public type TestConfig record {
    string s;
    int i;
    map<string> m;
    !...;
};

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
