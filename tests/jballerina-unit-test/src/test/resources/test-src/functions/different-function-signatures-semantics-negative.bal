function duplicateNamedParams(int a, float b, string c = "John", string c = "Doe", int... z) {
}

function functionAnyRestParam(any... z) returns (any[]) {
    return z;
}

function testInvokeFuncWithAnyRestParam1() returns (any[]) {
    int[] a = [10, 20, 30];
    return functionAnyRestParam(...a);
}

function foo(string a= "John", string b = "Doe") {
}

function funcInvocWithDuplicateNamedArgs() {
    foo(a="Alex", a="Bob");
}

function funcInvocWithNonExistingNamedArgs() {
    foo(a="Alex", c="Bob");
}

function bar(int a, string b = "John", int... z) {
}

function funcInvocWithInvalidIndividualRestArgWithVarArg() {
    int[] array = [1, 2, 3];
    bar(5, "Alex", 6.0, ...array);
}

//function funcInvocAsRestArgs() returns [int, float, string, int, string, int[]] { // moved to different-function-signatures-negative.bal
//    bar(5, "Alex", 6, ...getIntArrayAndStringTuple());
//}

function getIntArrayAndStringTuple() returns [int[], string] {
    return [[1,2,3,4], "hello"];
}

function funcWithDefaultParamIncompatibleType(json j = xml `{"name":"John"}`) returns json {
    return j;
}

function funcWithComplexDefaultParamExpr(json j = {"name":"John"}) returns json {
    return j;
}

function functionWithOnlyPositionalParams(int a, boolean b, string c) returns int {
    return a;
}

function functionWithOnlyDefaultableParams(int a = 1, boolean b = true, string c = "string") returns int {
    return a;
}

function testCallingFunctionWithOnlyPositionalParams() {
    _ = functionWithOnlyPositionalParams(); // not enough arguments in call to 'functionWithOnlyPositionalParams()'
    _ = functionWithOnlyPositionalParams(1, true); // not enough arguments in call to 'functionWithOnlyPositionalParams()'
    _ = functionWithOnlyPositionalParams(1, "c"); // not enough arguments in call to 'functionWithOnlyPositionalParams()'
    _ = functionWithOnlyPositionalParams(a = 1, c = "c"); // not enough arguments in call to 'functionWithOnlyPositionalParams()'
    _ = functionWithOnlyPositionalParams(1, true, "c", "d"); // too many arguments in call to 'functionWithOnlyPositionalParams()'
    _ = functionWithOnlyPositionalParams(1, true, "c", c = true); // too many arguments in call to 'functionWithOnlyPositionalParams()'
    _ = functionWithOnlyPositionalParams(1, a = 2, b = true, c = "c"); // too many arguments in call to 'functionWithOnlyPositionalParams()'
}

function testCallingFunctionWithOnlyDefaultableParams() {
    _ = functionWithOnlyDefaultableParams(1, true);
    _ = functionWithOnlyDefaultableParams(1, "c"); // incompatible types: expected 'boolean', found 'string'
    _ = functionWithOnlyDefaultableParams(a = 1, c = "c");
    _ = functionWithOnlyDefaultableParams(1, true, "c", "d"); // too many arguments in call to 'functionWithOnlyDefaultableParams()'
    _ = functionWithOnlyDefaultableParams(1, true, "c", c = true); // too many arguments in call to 'functionWithOnlyDefaultableParams()'
    _ = functionWithOnlyDefaultableParams(1, a = 2, b = true, c = "c"); // too many arguments in call to 'functionWithOnlyDefaultableParams()'
}

function positionalAfterDefaultable(int x, string y = "s", boolean b) { // required parameter not allowed after defaultable parameters

}

function normalFunction(int x, string y, float f = 1.1, boolean... b) {

}

function restArgTest() {
    boolean[] bArray = [true, false, true];
    normalFunction(1, "A", 2.2, bArray); // incompatible types: expected 'boolean', found 'boolean[]'
    normalFunction(1, "A", 2.2, ...bArray);
    normalFunction(1, "A", bArray); // incompatible types: expected 'float', found 'boolean[]'
    normalFunction(1, "A", ...bArray); // incompatible types: expected '[float,boolean...]', found 'boolean[]'
    normalFunction(x = 1, y = "A", f = 2.2, bArray); // positional argument not allowed after named arguments
    normalFunction(x = 1, y = "A", f = 2.2, ...bArray); // rest argument not allowed after named arguments
}

function functionWithNoRestParam(int x, string y, float f = 1.1, boolean b = true) {

}

function requiredParamTest() {
    functionWithNoRestParam(100); // not enough arguments in call to 'functionWithNoRestParam()'
    functionWithNoRestParam("string"); // not enough arguments in call to 'functionWithNoRestParam()'
    functionWithNoRestParam("string", 100); // incompatible types
    functionWithNoRestParam(100, "string");
    functionWithNoRestParam(100, "string", 2.2);
    functionWithNoRestParam(100, "string", false); // incompatible types: expected 'float', found 'boolean'
    functionWithNoRestParam(100, "string", 2.2, false);
    functionWithNoRestParam(100, "string", false, 2.2); // incompatible types:
    functionWithNoRestParam(); // not enough arguments in call to 'functionWithNoRestParam()'
    functionWithNoRestParam(f = 1.2); // not enough arguments in call to 'functionWithNoRestParam()'
    functionWithNoRestParam(b = false, f = 1.2); // missing required parameter 'x' in call to 'functionWithNoRestParam'()
    // missing required parameter 'y' in call to 'functionWithNoRestParam'()
}

function testDefaultExprEvaluation() {
    functionWithNoRestParam(1, "2", f = getFloat());
    functionWithNoRestParam(1, f = getFloat()); // missing required parameter 'y' in call to 'functionWithNoRestParam'()
    functionWithNoRestParam(1, f = getFloat(2.2)); // too many arguments in call to 'getFloat()'
}

function getFloat() returns float {
    return 25.0;
}

function testForwardReferencingParams1(int x, int y = z, int z = 12) { } // undefined symbol 'z'
function testForwardReferencingParams2(float y = z * 2, float z = getFloat()) { } // undefined symbol 'z'

class Foo {
    function testForwardReferencingParams1(int x, int y = z, int z = 12) { } // undefined symbol 'z'
    function testForwardReferencingParams2(float y = z * 2, float z = getFloat()) { } // undefined symbol 'z'
}
