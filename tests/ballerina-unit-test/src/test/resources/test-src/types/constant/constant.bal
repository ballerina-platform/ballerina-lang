import ballerina/reflect;

const nameWithoutType = "Ballerina";
const string nameWithType = "Ballerina";

const nilWithoutType = ();

function testNilWithoutType() returns () {
    return nilWithoutType;
}

const () nilWithType = ();

function testNilWithType() returns () {
    return nilWithType;
}

// -----------------------------------------------------------

function testConstWithTypeInReturn() returns string {
    return nameWithType;
}

// -----------------------------------------------------------

function testConstWithoutTypeInReturn() returns string {
    return nameWithType;
}

// -----------------------------------------------------------

function testConstWithTypeAsParam() returns string {
    return testParam(nameWithType);
}

function testConstWithoutTypeAsParam() returns string {
    return testParam(nameWithoutType);
}

function testParam(string s) returns string {
    return s;
}

// -----------------------------------------------------------

type Data record {
    string firstName;
};

function testConstInRecord() returns string {
    Data d = { firstName: "Ballerina" };
    return d.firstName;
}

// -----------------------------------------------------------

string sgvWithType = nameWithType;

function testConstWithTypeAssignmentToGlobalVariable() returns string {
    return sgvWithType;
}

function testConstWithTypeAssignmentToLocalVariable() returns string {
    string slv = nameWithType;
    return slv;
}

// -----------------------------------------------------------

string sgvWithoutType = nameWithoutType;

function testConstWithoutTypeAssignmentToGlobalVariable() returns string {
    return sgvWithoutType;
}

function testConstWithoutTypeAssignmentToLocalVariable() returns string {
    string slv = nameWithoutType;
    return slv;
}

// -----------------------------------------------------------

function testConstWithTypeConcat() returns string {
    return nameWithType + " rocks";
}

// -----------------------------------------------------------

function testConstWithoutTypeConcat() returns string {
    return nameWithoutType + " rocks";
}

// -----------------------------------------------------------

type ACTION "GET"|"POST";

const GET = "GET";
const POST = "POST";

function testTypeConstants() returns ACTION {
    return GET;
}

const constActionWithType = "GET";

function testConstWithTypeAssignmentToType() returns ACTION {
    ACTION action = constActionWithType;
    return action;
}

const constActionWithoutType = "GET";

function testConstWithoutTypeAssignmentToType() returns ACTION {
    ACTION action = constActionWithoutType;
    return action;
}

function testConstAndTypeComparison() returns boolean {
    return "GET" == GET;
}

function testTypeConstAsParam() returns boolean {
    return typeConstAsParam(GET);
}

function typeConstAsParam(ACTION a) returns boolean {
    return "GET" == a;
}

// -----------------------------------------------------------

function testEqualityWithConstWithType() returns boolean {
    return nameWithType == "Ballerina";
}

// -----------------------------------------------------------

const boolean conditionWithType = true;

function testConstWithTypeInCondition() returns boolean {
    if (conditionWithType) {
        return true;
    }
    return false;
}

// -----------------------------------------------------------

const conditionWithoutType = true;

function testConstWithoutTypeInCondition() returns boolean {
    if (conditionWithoutType) {
        return true;
    }
    return false;
}

// -----------------------------------------------------------

const boolean booleanWithType = false;

function testBooleanWithType() returns boolean {
    return booleanWithType;
}

const booleanWithoutType = true;

function testBooleanWithoutType() returns boolean {
    return booleanWithoutType;
}

// -----------------------------------------------------------

const int intWithType = 40;

function testIntWithType() returns int {
    return intWithType;
}

const intWithoutType = 20;

function testIntWithoutType() returns int {
    return intWithoutType;
}

// -----------------------------------------------------------

const byte byteWithType = 240;

function testByteWithType() returns byte {
    return byteWithType;
}

// -----------------------------------------------------------

const float floatWithType = 4.0;

function testFloatWithType() returns float {
    return floatWithType;
}

const floatWithoutType = 2.0;

function testFloatWithoutType() returns float {
    return floatWithoutType;
}

// -----------------------------------------------------------

type FiniteFloatType floatWithType|floatWithoutType;

function testFloatAsFiniteType() returns (FiniteFloatType, FiniteFloatType) {
    FiniteFloatType f1 = 2.0;
    FiniteFloatType f2 = 4.0;

    return (f1, f2);
}

// -----------------------------------------------------------

const decimal decimalWithType = 50.0;

function testDecimalWithType() returns decimal {
    return decimalWithType;
}

// -----------------------------------------------------------

const string stringWithType = "Ballerina is awesome";

function testStringWithType() returns string {
    return stringWithType;
}

const stringWithoutType = "Ballerina rocks";

function testStringWithoutType() returns string {
    return stringWithoutType;
}

// -----------------------------------------------------------

const string key = "key";
const string value = "value";

function testConstInMapKey() returns string {
    map<string> m = { key: "value" };
    return m.key;
}

function testConstInMapValue() returns string {
    map<string> m = { "key": value };
    return m.key;
}

function testConstInJsonKey() returns json {
    json j = { key: "value" };
    return j.key;
}

function testConstInJsonValue() returns json {
    json j = { "key": value };
    return j.key;
}

// -----------------------------------------------------------

function testBooleanConstInUnion() returns any {
    boolean|int v = booleanWithType;
    return v;
}

function testIntConstInUnion() returns any {
    int|boolean v = intWithType;
    return v;
}

function testByteConstInUnion() returns any {
    byte|boolean v = byteWithType;
    return v;
}

function testFloatConstInUnion() returns any {
    float|boolean v = floatWithType;
    return v;
}

function testDecimalConstInUnion() returns any {
    decimal|boolean v = decimalWithType;
    return v;
}

function testStringConstInUnion() returns any {
    string|boolean v = stringWithType;
    return v;
}

// -----------------------------------------------------------

function testBooleanConstInTuple() returns boolean {
    (boolean, int) v = (booleanWithType, 1);
    return v[0];
}

function testIntConstInTuple() returns int {
    (int, boolean) v = (intWithType, true);
    return v[0];
}

function testByteConstInTuple() returns byte {
    (byte, boolean) v = (byteWithType, true);
    return v[0];
}

function testFloatConstInTuple() returns float {
    (float, boolean) v = (floatWithType, true);
    return v[0];
}

function testDecimalConstInTuple() returns decimal {
    (decimal, boolean) v = (decimalWithType, true);
    return v[0];
}

function testStringConstInTuple() returns string {
    (string, boolean) v = (stringWithType, true);
    return v[0];
}

// -----------------------------------------------------------

const D = "D";

const E = "E";

const F = "F";

type G D|E|F;

type H D|E;

const h = "D";

function testProperSubset() returns G {
    G g = h;
    return g;
}

// -----------------------------------------------------------

const string SHA1 = "SHA1";

function testBuiltinFunctionInvocation() returns boolean {
    return SHA1.equalsIgnoreCase("SHA1");
}

// -----------------------------------------------------------

function testBuiltinFunctionInvocationOnArrayElement() returns boolean {
    string[] arr = [SHA1];
    return arr[0].equalsIgnoreCase("SHA1");
}

// -----------------------------------------------------------

type TestRecord record {
    string field;
};

function testBuiltinFunctionInvocationOnField() returns boolean {
    TestRecord tr = { field: SHA1 };
    return tr.field.equalsIgnoreCase("SHA1");
}
// -----------------------------------------------------------

type STRING_LABEL string;

const STRING_LABEL labeledString = "Ballerina";

function testLabeling() returns string {
    return labeledString;
}

// -----------------------------------------------------------

// Todo - Enable after fixing https://github.com/ballerina-platform/ballerina-lang/issues/11183.
//type M record { string f; }|Z;
//
//Z z1 = "V";
//
//Z z2 = "W";
//
//Z z3 = "X";
//
//
//const string W = "W";
//
//const string X = "X";

// -----------------------------------------------------------

const aBoolean = true;

function testBooleanConcat() returns string {
    return aBoolean + " rocks";
}

const aInt = 24;

function testIntConcat() returns string {
    return aInt + " rocks";
}

const aByte = 12;

function testByteConcat() returns string {
    return aByte + " rocks";
}

const aFloat = 25.5;

function testFloatConcat() returns string {
    return aFloat + " rocks";
}

const decimal aDecimal = 25.5;

function testDecimalConcat() returns string {
    return aDecimal + " rocks";
}

// -----------------------------------------------------------

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

// -----------------------------------------------------------

map<map<string>> m4 = { "m4k": m5 };
const map<string> m5 = { "m5k": "m5v" };

function updateNestedConstantMapValue() {
    m4.m4k.m5k = "m5nv";
}

// -----------------------------------------------------------

map<string>[] a1 = [m5];

function updateConstantMapValueInArray() {
    a1[0].mk5 = "m5nv";
}

