import testorg/foo;

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

function testBooleanConstKeyReference() returns map<boolean> {
    return foo:bm5;
}

function testIntConstKeyReference() returns map<int> {
    return foo:im5;
}

function testByteConstKeyReference() returns map<byte> {
    return foo:bytem5;
}

function testFloatConstKeyReference() returns map<float> {
    return foo:fm5;
}

function testDecimalConstKeyReference() returns map<decimal> {
    return foo:dm5;
}

function testStringConstKeyReference() returns map<string> {
    return foo:sm5;
}

function testNullConstKeyReference() returns map<()> {
    return foo:nm5;
}

// -----------------------------------------------------------

function testBooleanConstKeyReferenceInLocalVar() returns boolean {
    boolean b = <boolean>foo:bm4["bm4k"];
    return b;
}

function testIntConstKeyReferenceInLocalVar() returns int {
    int i = <int>foo:im4["im4k"];
    return i;
}

function testByteConstKeyReferenceInLocalVar() returns byte {
    byte b = <byte>foo:bytem4["bytem4k"];
    return b;
}

function testFloatConstKeyReferenceInLocalVar() returns float {
    float f = <float>foo:fm4["fm4k"];
    return f;
}

function testDecimalConstKeyReferenceInLocalVar() returns decimal {
    decimal d = <decimal>foo:dm4["dm4k"];
    return d;
}

function testStringConstKeyReferenceInLocalVar() returns string {
    string s = <string>foo:sm4["sm4k"];
    return s;
}

function testNullConstKeyReferenceInLocalVar() returns () {
    () n = <()>foo:nm4["nm4k"];
    return n;
}

// annotations -----------------------------------------------

@foo:testAnnotation {
    s: foo:sConst,
    i: foo:iConst,
    m: foo:mConst
}
type RecordOne record {

};

function testConstInAnnotations() returns foo:TestConfig? {
    RecordOne r1 = {};
    typedesc<RecordOne> t = typeof r1;
    return t.@foo:testAnnotation;
}
