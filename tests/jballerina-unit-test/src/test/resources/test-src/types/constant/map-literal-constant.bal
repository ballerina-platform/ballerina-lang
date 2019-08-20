const map<map<boolean>> bm3 = { "key5": bm1, "key6": bm2, "key7": { "key8": true, "key9": false }};
const map<boolean> bm1 = { "key1": true, "key2": false };
const map<boolean> bm2 = { "key3": false, "key4": true };

function testSimpleBooleanConstMap() returns map<boolean> {
    return bm1;
}

function testComplexBooleanConstMap() returns map<map<boolean>> {
    return bm3;
}

// -----------------------------------------------------------

const map<map<int>> im3 = { "key5": im1, "key6": im2, "key7:": { "key8": 8, "key9": 9 }};
const map<int> im1 = { "key1": 1, "key2": 2 };
const map<int> im2 = { "key3": 3, "key4": 4 };

function testSimpleIntConstMap() returns map<int> {
    return im1;
}

function testComplexIntConstMap() returns map<map<int>> {
    return im3;
}

// -----------------------------------------------------------

const map<map<byte>> bytem3 = { "key5": bytem1, "key6": bytem2, "key7": { "key8": 80, "key9": 90 }};
const map<byte> bytem1 = { "key1": 10, "key2": 20 };
const map<byte> bytem2 = { "key3": 30, "key4": 40 };

function testSimpleByteConstMap() returns map<byte> {
    return bytem1;
}

function testComplexByteConstMap() returns map<map<byte>> {
    return bytem3;
}

// -----------------------------------------------------------

const map<map<float>> fm3 = { "key5": fm1, "key6": fm2, "key7": { "key8": 8.0, "key9": 9.0 }};
const map<float> fm1 = { "key1": 1.0, "key2": 2.0 };
const map<float> fm2 = { "key3": 3.0, "key4": 4.0 };

function testSimpleFloatConstMap() returns map<float> {
    return fm1;
}

function testComplexFloatConstMap() returns map<map<float>> {
    return fm3;
}

// -----------------------------------------------------------

const map<map<decimal>> dm3 = { "key5": dm1, "key6": dm2, "key7": { "key8": 800, "key9": 900 }};
const map<decimal> dm1 = { "key1": 100, "key2": 200 };
const map<decimal> dm2 = { "key3": 300, "key4": 400 };

function testSimpleDecimalConstMap() returns map<decimal> {
    return dm1;
}

function testComplexDecimalConstMap() returns map<map<decimal>> {
    return dm3;
}

// -----------------------------------------------------------

const map<map<string>> sm3 = { "key5": sm1, "key6": sm2, "key7": { "key8": "value8", "key9": "value9" }};
const map<string> sm1 = { "key1": "value1", "key2": "value2" };
const map<string> sm2 = { "key3": "value3", "key4": "value4" };

function testSimpleStringConstMap() returns map<string> {
    return sm1;
}

function testComplexStringConstMap() returns map<map<string>> {
    return sm3;
}

// -----------------------------------------------------------

const map<map<()>> nm3 = { "key5": nm1, "key6": nm2, "key7": { "key8": (), "key9": () }};
const map<()> nm1 = { "key1": (), "key2": () };
const map<()> nm2 = { "key3": (), "key4": () };

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
map<boolean?> bm5 = { "bm5kn": bm4["bm4k"] };

function testBooleanMapConstMemberAccess() returns map<boolean?> {
    return bm5;
}

// -----------------------------------------------------------

const map<int> im4 = { "im4k": 123 };
map<int?> im5 = { "im5kn": im4["im4k"] };

function testIntMapConstMemberAccess() returns map<int?> {
    return im5;
}

// -----------------------------------------------------------

const map<byte> bytem4 = { "bytem4k": 64 };
map<byte?> bytem5 = { "bytem5kn": bytem4["bytem4k"] };

function testByteMapConstMemberAccess() returns map<byte?> {
    return bytem5;
}

// -----------------------------------------------------------

const map<float> fm4 = { "fm4k": 12.5 };
map<float?> fm5 = { "fm5kn": fm4["fm4k"] };

function testFloatMapConstMemberAccess() returns map<float?> {
    return fm5;
}

// -----------------------------------------------------------

const map<decimal> dm4 = { "dm4k": 5.56 };
map<decimal?> dm5 = { "dm5kn": dm4["dm4k"] };

function testDecimalMapConstMemberAccess() returns map<decimal?> {
    return dm5;
}

// -----------------------------------------------------------

const map<string> sm4 = { "sm4k": "sm4v" };
map<string?> sm5 = { "sm5kn": sm4["sm4k"] };

function testStringMapConstMemberAccess() returns map<string?> {
    return sm5;
}

// -----------------------------------------------------------

const map<()> nm4 = { "nm4k": () };
map<()> nm5 = { "nm5kn": nm4["nm4k"] };

function testNilMapConstMemberAccess() returns map<()> {
    return nm5;
}

// -----------------------------------------------------------

function testBooleanConstMemberAccessInLocalVar() returns boolean {
    boolean? b = bm4["bm4k"];
    return <boolean> b;
}

// -----------------------------------------------------------

function testIntConstMemberAccessInLocalVar() returns int {
    int? i = im4["im4k"];
    return <int> i;
}

// -----------------------------------------------------------

function testByteConstMemberAccessInLocalVar() returns byte {
    byte? b = bytem4["bytem4k"];
    return <byte> b;
}

// -----------------------------------------------------------

function testFloatConstMemberAccessInLocalVar() returns float {
    float? f = fm4["fm4k"];
    return <float> f;
}

// -----------------------------------------------------------

function testDecimalConstMemberAccessInLocalVar() returns decimal {
    decimal? d = dm4["dm4k"];
    return <decimal> d;
}

// -----------------------------------------------------------

function testStringConstMemberAccessInLocalVar() returns string {
    string? s = sm4["sm4k"];
    return <string> s;
}

// -----------------------------------------------------------

function testNullConstMemberAccessInLocalVar() returns () {
    () n = nm4["nm4k"];
    return n;
}

// annotations -----------------------------------------------

const sConst = "Ballerina";
const iConst = 100;
const map<string> mConst = { "mKey": "mValue" };

public type TestConfig record {|
    string s;
    int i;
    map<string> m;
|};

public annotation TestConfig testAnnotation on type;

@testAnnotation {
    s: sConst,
    i: iConst,
    m: mConst
}
type RecordOne record {

};

function testConstInAnnotations() returns TestConfig? {
    RecordOne r1 = {};
    typedesc<any> t = typeof r1;
    return t.@testAnnotation;
}

const map<string> data = { "user": "Ballerina", "ID": "1234" };
const map<map<string>> complexData = { "data": data, "moreData": { "user": "WSO2" } };

function testNestedConstMapAccess() returns boolean {
    return complexData["data"]["user"] == "Ballerina" && complexData["data"]["ID"] == "1234";
}
