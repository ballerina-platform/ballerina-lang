import testorg/foo;

// -----------------------------------------------------------

function testNilWithoutType() returns () {
    return foo:NilWithoutType;
}

function testNilWithType() returns () {
    return foo:NilWithType;
}

// -----------------------------------------------------------

function testConstWithTypeInReturn() returns string {
    return foo:nameWithType;
}

// -----------------------------------------------------------

function testConstWithoutTypeInReturn() returns string {
    return foo:nameWithType;
}

// -----------------------------------------------------------

function testConstWithTypeAsParam() returns string {
    return testParam(foo:nameWithType);
}

function testConstWithoutTypeAsParam() returns string {
    return testParam(foo:nameWithoutType);
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

function testConstWithTypeAssignmentToGlobalVariable() returns string {
    return foo:getSgvWithType();
}

function testConstWithTypeAssignmentToLocalVariable() returns string {
    string slv = foo:nameWithType;
    return slv;
}

// -----------------------------------------------------------


function testConstWithoutTypeAssignmentToGlobalVariable() returns string {
    return foo:getSgvWithoutType();
}

function testConstWithoutTypeAssignmentToLocalVariable() returns string {
    string slv = foo:nameWithoutType;
    return slv;
}

// -----------------------------------------------------------

function testConstWithTypeConcat() returns string {
    return foo:nameWithType + " rocks";
}

// -----------------------------------------------------------

function testConstWithoutTypeConcat() returns string {
    return foo:nameWithoutType + " rocks";
}

// -----------------------------------------------------------

function testTypeConstants() returns foo:ACTION {
    return foo:GET;
}

function testConstWithTypeAssignmentToType() returns foo:ACTION {
    foo:ACTION action = foo:constActionWithType;
    return action;
}


function testConstWithoutTypeAssignmentToType() returns foo:ACTION {
    foo:ACTION action = foo:constActionWithoutType;
    return action;
}

function testConstAndTypeComparison() returns boolean {
    return "GET" == foo:GET;
}

function testTypeConstAsParam() returns boolean {
    return typeConstAsParam(foo:GET);
}

function typeConstAsParam(foo:ACTION a) returns boolean {
    return "GET" == a;
}

// -----------------------------------------------------------

function testEqualityWithConstWithType() returns boolean {
    return foo:nameWithType == "Ballerina";
}

// -----------------------------------------------------------

function testConstWithTypeInCondition() returns boolean {
    if (foo:conditionWithType) {
        return true;
    }
    return false;
}

// -----------------------------------------------------------

function testConstWithoutTypeInCondition() returns boolean {
    if (foo:conditionWithoutType) {
        return true;
    }
    return false;
}

// -----------------------------------------------------------

function testBooleanWithType() returns boolean {
    return foo:booleanWithType;
}

function testBooleanWithoutType() returns boolean {
    return foo:booleanWithoutType;
}

// -----------------------------------------------------------

function testIntWithType() returns int {
    return foo:intWithType;
}

function testIntWithoutType() returns int {
    return foo:intWithoutType;
}

// -----------------------------------------------------------

function testByteWithType() returns byte {
    return foo:byteWithType;
}

// -----------------------------------------------------------

function testFloatWithType() returns float {
    return foo:floatWithType;
}

function testFloatWithoutType() returns float {
    return foo:floatWithoutType;
}

// -----------------------------------------------------------

function testDecimalWithType() returns decimal {
    return foo:decimalWithType;
}

// -----------------------------------------------------------

function testStringWithType() returns string {
    return foo:stringWithType;
}

function testStringWithoutType() returns string {
    return foo:stringWithoutType;
}

// -----------------------------------------------------------

function testFloatAsFiniteType() returns [foo:FiniteFloatType, foo:FiniteFloatType] {
    foo:FiniteFloatType f1 = 2.0;
    foo:FiniteFloatType f2 = 4.0;

    return [f1, f2];
}

// -----------------------------------------------------------

function testConstInMapKey() returns string {
    string key = foo:KEY;
    map<string> m = { key: "value" };
    return <string>m["key"];
}

function testConstInMapValue() returns string {
    string value = foo:VALUE;
    map<string> m = { "key": value };
    return <string>m["key"];
}

function testConstInJsonKey() returns json {
    string key = foo:KEY;
    json j = { key: "value" };
    return <json>j.key;
}

function testConstInJsonValue() returns json {
    string value = foo:VALUE;
    json j = { "key": value };
    return <json>j.key;
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
    [boolean, int] v = [foo:booleanWithType, 1];
    return v[0];
}

function testIntConstInTuple() returns int {
    [int, boolean] v = [foo:intWithType, true];
    return v[0];
}

function testByteConstInTuple() returns byte {
    [byte, boolean] v = [foo:byteWithType, true];
    return v[0];
}

function testFloatConstInTuple() returns float {
    [float, boolean] v = [foo:floatWithType, true];
    return v[0];
}

function testStringConstInTuple() returns string {
    [string, boolean] v = [foo:stringWithType, true];
    return v[0];
}

// -----------------------------------------------------------

function testProperSubset() returns foo:G {
    foo:G g = foo:h;
    return g;
}

// -----------------------------------------------------------

function testBuiltinFunctionInvocation() returns boolean {
    return foo:SHA1.toUpperAscii() == "SHA1";
}

// -----------------------------------------------------------

function testBuiltinFunctionInvocationOnArrayElement() returns boolean {
    string[] arr = [foo:SHA1];
    return arr[0].toUpperAscii() == "SHA1";
}

// -----------------------------------------------------------

function testBuiltinFunctionInvocationOnField() returns boolean {
    foo:TestRecord tr = { field: foo:SHA1 };
    return tr.field.toUpperAscii() == "SHA1";
}

// -----------------------------------------------------------

function testLabeling() returns string {
    return foo:labeledString;
}
