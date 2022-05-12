import ballerina/jballerina.java;
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

function testConstTypesInline() {
    1 _ = foo:XCONST; // OK
    anydata a = foo:XCONST;
    assertTrue(a is 1);
    assertEqual(1, a);

    readonly & record {| 1 a; 2 b; |} _ = foo:BCONST; // OK
    anydata b = foo:BCONST;
    assertTrue(b is readonly & record {| 1 a; 2 b; |});
    assertEqual({a: 1, b: 2}, b);

    readonly & record {| record {| 1 a; 2 b; |} a; record {| 3 a; |} b; |} _ = foo:CCONST; // OK
    anydata c = foo:CCONST;
    assertTrue(c is readonly & record {| record {| 1 a; 2 b; |} a; record {| 3 a; |} b; |});
    assertEqual({a: {a: 1, b: 2}, b: {a: 3}}, c);
}

function testInvalidRuntimeUpdateOfConstMaps() {
    map<int> a = foo:BCONST;

    function () fn = function () {
        a["a"] = 1;
    };
    error? res = trap fn();
    // TODO: replace with an equality check once https://github.com/ballerina-platform/ballerina-lang/issues/34808 is fixed.
    assertInvalidUpdateError(res, "cannot update 'readonly' field 'a' in record of type 'foo\\:\\(testorg\\/foo\\:1\\:\\$anonType\\$BCONST\\$ & readonly\\)'");

    record {| 1 a; 2 b; |} b = foo:CCONST.a;
    fn = function () {
        b.b = 2;
    };
    res = trap fn();
    assertInvalidUpdateError(res, "cannot update 'readonly' field 'b' in record of type 'foo\\:\\(testorg\\/foo\\:1\\:\\$anonType\\$BCONST\\$ & readonly\\)'");

    map<map<int>> c = foo:CCONST;
    fn = function () {
        c["a"]["a"] = 2;
    };
    res = trap fn();
    assertInvalidUpdateError(res, "cannot update 'readonly' field 'a' in record of type 'foo\\:\\(testorg\\/foo\\:1\\:\\$anonType\\$BCONST\\$ & readonly\\)'");

    fn = function () {
        c["c"] = {};
    };
    res = trap fn();
    // https://github.com/ballerina-platform/ballerina-lang/issues/34798
    assertInvalidUpdateError(res, "invalid value for record field 'c': expected value of type 'never', found 'map<int>'");

    fn = function () {
        c["a"] = {a: 1, b: 2};
    };
    res = trap fn();
    assertInvalidUpdateError(res, "cannot update 'readonly' field 'a' in record of type 'foo\\:\\(testorg\\/foo\\:1\\:\\$anonType\\$CCONST\\$ & readonly\\)'");
}

function assertInvalidUpdateError(error? res, string expectedDetailMessage) {
    assertTrue(res is error);
    error err = <error> res;
    assertEqual("{ballerina/lang.map}InherentTypeViolation", err.message());
    assertTrue(matches(<string> checkpanic err.detail()["message"], expectedDetailMessage));
}

function assertTrue(anydata actual) {
    assertEqual(true, actual);
}

function assertEqual(anydata expected, anydata actual) {
    if expected == actual {
        return;
    }
    panic error(string `expected '${expected.toBalString()}', found '${actual.toBalString()}'`);
}

isolated function matches(string stringToMatch, string regex) returns boolean {
    return matchesExternal(java:fromString(stringToMatch), java:fromString(regex));
}

isolated function matchesExternal(handle stringToMatch, handle regex) returns boolean = @java:Method {
    name: "matches",
    'class: "java.lang.String",
    paramTypes: ["java.lang.String"]
} external;
