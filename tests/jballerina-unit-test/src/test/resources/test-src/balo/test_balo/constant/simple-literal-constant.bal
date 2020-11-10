import testorg/test_project;

// -----------------------------------------------------------

function testNilWithoutType() returns () {
    return test_project:NilWithoutType;
}

function testNilWithType() returns () {
    return test_project:NilWithType;
}

// -----------------------------------------------------------

function testConstWithTypeInReturn() returns string {
    return test_project:nameWithType;
}

// -----------------------------------------------------------

function testConstWithoutTypeInReturn() returns string {
    return test_project:nameWithType;
}

// -----------------------------------------------------------

function testConstWithTypeAsParam() returns string {
    return testParam(test_project:nameWithType);
}

function testConstWithoutTypeAsParam() returns string {
    return testParam(test_project:nameWithoutType);
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
    return test_project:getSgvWithType();
}

function testConstWithTypeAssignmentToLocalVariable() returns string {
    string slv = test_project:nameWithType;
    return slv;
}

// -----------------------------------------------------------


function testConstWithoutTypeAssignmentToGlobalVariable() returns string {
    return test_project:getSgvWithoutType();
}

function testConstWithoutTypeAssignmentToLocalVariable() returns string {
    string slv = test_project:nameWithoutType;
    return slv;
}

// -----------------------------------------------------------

function testConstWithTypeConcat() returns string {
    return test_project:nameWithType + " rocks";
}

// -----------------------------------------------------------

function testConstWithoutTypeConcat() returns string {
    return test_project:nameWithoutType + " rocks";
}

// -----------------------------------------------------------

function testTypeConstants() returns test_project:ACTION {
    return test_project:GET;
}

function testConstWithTypeAssignmentToType() returns test_project:ACTION {
    test_project:ACTION action = test_project:constActionWithType;
    return action;
}


function testConstWithoutTypeAssignmentToType() returns test_project:ACTION {
    test_project:ACTION action = test_project:constActionWithoutType;
    return action;
}

function testConstAndTypeComparison() returns boolean {
    return "GET" == test_project:GET;
}

function testTypeConstAsParam() returns boolean {
    return typeConstAsParam(test_project:GET);
}

function typeConstAsParam(test_project:ACTION a) returns boolean {
    return "GET" == a;
}

// -----------------------------------------------------------

function testEqualityWithConstWithType() returns boolean {
    return test_project:nameWithType == "Ballerina";
}

// -----------------------------------------------------------

function testConstWithTypeInCondition() returns boolean {
    if (test_project:conditionWithType) {
        return true;
    }
    return false;
}

// -----------------------------------------------------------

function testConstWithoutTypeInCondition() returns boolean {
    if (test_project:conditionWithoutType) {
        return true;
    }
    return false;
}

// -----------------------------------------------------------

function testBooleanWithType() returns boolean {
    return test_project:booleanWithType;
}

function testBooleanWithoutType() returns boolean {
    return test_project:booleanWithoutType;
}

// -----------------------------------------------------------

function testIntWithType() returns int {
    return test_project:intWithType;
}

function testIntWithoutType() returns int {
    return test_project:intWithoutType;
}

// -----------------------------------------------------------

function testByteWithType() returns byte {
    return test_project:byteWithType;
}

// -----------------------------------------------------------

function testFloatWithType() returns float {
    return test_project:floatWithType;
}

function testFloatWithoutType() returns float {
    return test_project:floatWithoutType;
}

// -----------------------------------------------------------

function testDecimalWithType() returns decimal {
    return test_project:decimalWithType;
}

// -----------------------------------------------------------

function testStringWithType() returns string {
    return test_project:stringWithType;
}

function testStringWithoutType() returns string {
    return test_project:stringWithoutType;
}

// -----------------------------------------------------------

function testFloatAsFiniteType() returns [test_project:FiniteFloatType, test_project:FiniteFloatType] {
    test_project:FiniteFloatType f1 = 2.0;
    test_project:FiniteFloatType f2 = 4.0;

    return [f1, f2];
}

// -----------------------------------------------------------

function testConstInMapKey() returns string {
    string key = test_project:KEY;
    map<string> m = { key: "value" };
    return <string>m["key"];
}

function testConstInMapValue() returns string {
    string value = test_project:VALUE;
    map<string> m = { "key": value };
    return <string>m["key"];
}

function testConstInJsonKey() returns json {
    string key = test_project:KEY;
    json j = { key: "value" };
    return <json>j.key;
}

function testConstInJsonValue() returns json {
    string value = test_project:VALUE;
    json j = { "key": value };
    return <json>j.key;
}

// -----------------------------------------------------------

function testBooleanConstInUnion() returns any {
    boolean|int v = test_project:booleanWithType;
    return v;
}

function testIntConstInUnion() returns any {
    int|boolean v = test_project:intWithType;
    return v;
}

function testByteConstInUnion() returns any {
    byte|boolean v = test_project:byteWithType;
    return v;
}

function testFloatConstInUnion() returns any {
    float|boolean v = test_project:floatWithType;
    return v;
}

function testStringConstInUnion() returns any {
    string|boolean v = test_project:stringWithType;
    return v;
}

// -----------------------------------------------------------

function testBooleanConstInTuple() returns boolean {
    [boolean, int] v = [test_project:booleanWithType, 1];
    return v[0];
}

function testIntConstInTuple() returns int {
    [int, boolean] v = [test_project:intWithType, true];
    return v[0];
}

function testByteConstInTuple() returns byte {
    [byte, boolean] v = [test_project:byteWithType, true];
    return v[0];
}

function testFloatConstInTuple() returns float {
    [float, boolean] v = [test_project:floatWithType, true];
    return v[0];
}

function testStringConstInTuple() returns string {
    [string, boolean] v = [test_project:stringWithType, true];
    return v[0];
}

// -----------------------------------------------------------

function testProperSubset() returns test_project:G {
    test_project:G g = test_project:h;
    return g;
}

// -----------------------------------------------------------

function testBuiltinFunctionInvocation() returns boolean {
    return test_project:SHA1.toUpperAscii() == "SHA1";
}

// -----------------------------------------------------------

function testBuiltinFunctionInvocationOnArrayElement() returns boolean {
    string[] arr = [test_project:SHA1];
    return arr[0].toUpperAscii() == "SHA1";
}

// -----------------------------------------------------------

function testBuiltinFunctionInvocationOnField() returns boolean {
    test_project:TestRecord tr = { 'field: test_project:SHA1 };
    return tr.'field.toUpperAscii() == "SHA1";
}

// -----------------------------------------------------------

function testLabeling() returns string {
    return test_project:labeledString;
}
