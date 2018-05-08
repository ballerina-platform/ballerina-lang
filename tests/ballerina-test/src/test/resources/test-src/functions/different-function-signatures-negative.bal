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

function funcInvocWitTooManyArgs() {
    int[] array = [1, 2, 3];
    bar(5, b="Alex", 6, ...array);
}

function funcInvocAsRestArgs() returns (int, float, string, int, string, int[]) {
    bar(5, b="Alex", 6, ...getIntArray());
}

function getIntArray() returns (int[], string) {
    return ([1,2,3,4], "hello");
}

function funcWithDefaultParamIncompatibleType(json j = xml `{"name":"John"}`) returns json {
    return j;
}

function funcWithComplexDefaultParamExpr(json j = {"name":"John"}) returns json {
    return j;
}
