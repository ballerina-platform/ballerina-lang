import testorg/foo version v1;

import ballerina/reflect;

type Ballerina "Ballerina";

function testAccessConstantWithoutType() returns Ballerina {
    return foo:constName;
}

function testAccessConstantWithoutTypeAsString() returns string {
    return foo:constName;
}

type Colombo "Colombo";

function testAccessConstantWithType() returns Colombo {
    return foo:constAddress;
}

type AB "A"|"B";

function testAccessFiniteType() returns foo:AB {
    return foo:A;
}

 function testReturnFiniteType() returns AB {
     return foo:A; // Valid because this is same as `return "A";`
 }

function testAccessTypeWithContInDef() returns foo:CD {
    foo:CD c = "C";
    return c;
}

// -----------------------------------------------------------

function testConstInMapKey() returns string {
    string key = foo:KEY;
    map<string> m = { key: "value" };
    return m.key;
}

function testConstInMapValue() returns string {
    string value = foo:VALUE;
    map<string> m = { "key": value };
    return m.key;
}

function testConstInJsonKey() returns json {
    string key = foo:KEY;
    json j = { key: "value" };
    return j.key;
}

function testConstInJsonValue() returns json {
    string value = foo:VALUE;
    json j = { "key": value };
    return j.key;
}

// -----------------------------------------------------------

function testBooleanConstInUnion() returns any {
    boolean|int v = foo:booleanWithType;
    return v;
}

function testIntConstInUnion() returns any {
    int|boolean v = foo:intWithType;
    return v;
}

function testByteConstInUnion() returns any {
    byte|boolean v = foo:byteWithType;
    return v;
}

function testFloatConstInUnion() returns any {
    float|boolean v = foo:floatWithType;
    return v;
}

function testStringConstInUnion() returns any {
    string|boolean v = foo:stringWithType;
    return v;
}

// -----------------------------------------------------------

function testBooleanConstInTuple() returns boolean {
    (boolean, int) v = (foo:booleanWithType, 1);
    return v[0];
}

function testIntConstInTuple() returns int {
    (int, boolean) v = (foo:intWithType, true);
    return v[0];
}

function testByteConstInTuple() returns byte {
    (byte, boolean) v = (foo:byteWithType, true);
    return v[0];
}

function testFloatConstInTuple() returns float {
    (float, boolean) v = (foo:floatWithType, true);
    return v[0];
}

function testStringConstInTuple() returns string {
    (string, boolean) v = (foo:stringWithType, true);
    return v[0];
}

// -----------------------------------------------------------

foo:CD test = "C";

function testTypeFromAnotherPackage() returns foo:CD {
    return test;
}

// -----------------------------------------------------------

// Todo - Enable after fixing https://github.com/ballerina-platform/ballerina-lang/issues/11183.
//type M record { string f; }|Z;
//
//M m1 = { f: "foo" };
//
//M m2 = "V";
//
//M m3 = "W";
//
//M m4 = "X";

type Y X;

type Z "V"|W|X;

Y y = "X";

Z z1 = "V";

Z z2 = "W";

Z z3 = "X";


const string W = "W";

const string X = "X";

// -----------------------------------------------------------

type BooleanTypeWithType foo:booleanWithType;

function testBooleanTypeWithType() returns BooleanTypeWithType {
    BooleanTypeWithType t = false;
    return t;
}

const booleanWithoutType = true;

type BooleanTypeWithoutType booleanWithoutType;

function testBooleanTypeWithoutType() returns BooleanTypeWithoutType {
    BooleanTypeWithoutType t = true;
    return t;
}

// -----------------------------------------------------------

type IntTypeWithType foo:intWithType;

function testIntTypeWithType() returns IntTypeWithType {
    IntTypeWithType t = 40;
    return t;
}


type IntTypeWithoutType foo:intWithoutType;

function testIntTypeWithoutType() returns IntTypeWithoutType {
    IntTypeWithoutType t = 20;
    return t;
}

// -----------------------------------------------------------

type ByteTypeWithType foo:byteWithType;

function testByteTypeWithType() returns ByteTypeWithType {
    ByteTypeWithType t = 240;
    return t;
}

// -----------------------------------------------------------

type FloatTypeWithType foo:floatWithType;

function testFloatTypeWithType() returns FloatTypeWithType {
    FloatTypeWithType t = 4.0;
    return t;
}

type FloatTypeWithoutType foo:floatWithoutType;

function testFloatTypeWithoutType() returns FloatTypeWithoutType {
    FloatTypeWithoutType t = 2.0;
    return t;
}

// -----------------------------------------------------------

type DecimalTypeWithType foo:decimalWithType;

function testDecimalTypeWithType() returns DecimalTypeWithType {
    DecimalTypeWithType t = 4.0;
    return t;
}

// -----------------------------------------------------------

type StringTypeWithType foo:stringWithType;

function testStringTypeWithType() returns StringTypeWithType {
    StringTypeWithType t = "Ballerina is awesome";
    return t;
}

type StringTypeWithoutType foo:stringWithoutType;

function testStringTypeWithoutType() returns StringTypeWithoutType {
    StringTypeWithoutType t = "Ballerina rocks";
    return t;
}

// -----------------------------------------------------------

function testFloatAsFiniteType() returns (foo:FiniteFloatType, foo:FiniteFloatType) {
    foo:FiniteFloatType f1 = 2.0;
    foo:FiniteFloatType f2 = 4.0;

    return (f1, f2);
}

// -----------------------------------------------------------

function testNilWithoutType() returns () {
    return foo:NilWithoutType;
}

function testNilWithType() returns () {
    return foo:NilWithType;
}

// -----------------------------------------------------------

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

function updateNestedConstantMapValue() {
    foo:m4.m4k.m5k = "m5nv";
}

function updateNestedConstantMapValue2() {
    foo:m4.m4k.newKey = "newValue";
}

// -----------------------------------------------------------

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
