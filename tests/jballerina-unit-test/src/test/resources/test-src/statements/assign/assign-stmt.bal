function testIntAssignStmt(int a) returns int {
    int x;
    x = a;
    return x;
}

function testFloatAssignStmt(float a) returns float {
    float x;
    x = a;
    return x;
}

function testBooleanAssignStmt(boolean a) returns boolean {
    boolean x;
    x = a;
    return x;
}

function testStringAssignStmt(string a) returns string {
    string x;
    x = a;
    return x;
}

function testIntToArrayAssignStmt(int a) returns int {
    int[] arr = [];
    arr[0] = a;
    return arr[0];
}

function testArrayIndexToIntAssignStmt(int[] arr) returns int {
    int a;
    a = arr[0];
    return a;
}

function testMultiReturn() returns [int, string, int] {
    int a;
    string name;
    int b;

    [a, name, b] = testMultiReturnInternal();
    return [a, name, b];
}

function testMultiReturnInternal() returns [int, string, int] {
    return [5, "john", 6];
}

function testIntCastFloatStmt (int a) returns float {
    float x;
    //Cannot directly assign int to float, need to convert
    x = <float>a;
    return x;
}

function testBinaryExpressionIntAndFloatStmt (int a) returns float {
    float x;
    x = <float>a;
    return x + a;
}

public client class Client {
    public remote function foo() returns [int, int] {
        return [0, 0];
    }

    public remote function foo1() returns record { string a; } {
        return { a: "a" };
    }

    public remote function foo2() returns error {
        return error("the error reason");
    }

    public remote function foo3() returns error {
        return error("foo3 error", failedAttempts = 3);
    }
}

public function restActionResultAssignment() returns [int, int, string, string, string, int] {
    Client c = new();
    var [a, b] = c->foo();
    var { a: d } = c->foo1();
    var error(r) = c->foo2();
    var error(r2, failedAttempts = failedAttempts) = c->foo3();
    return [a, b, d, r, r2, <int>failedAttempts];
}

function testAssignErrorArrayToAny() {
    string errorReason = "TestError";
    error testError = error(errorReason);
    error[] errorArray = [testError];
    any anyVal = errorArray;
    error[] errorArrayBack = <error[]>anyVal;
    assertEquality(errorReason, errorArrayBack[0].message());
}

function testAssignIntArrayToJson() {
    int[*] intArray = [1, 2];
    json jsonVar = intArray;
    assertTrue(jsonVar is int[2]);
    int[2] arr = <int[2]> jsonVar;
    assertEquality(1, arr[0]);
    assertEquality(2, arr[1]);
}

function testAssignIntOrStringArrayIntOrFloatOrStringUnionArray() {
    int[]|string[] arr1 = <int[]>[1, 2];
    (int|float)[]|string[] arr2 = arr1;
    assertEquality(1, arr2[0]);
    assertEquality(2, arr2[1]);
}

function assignAnyToUnionWithErrorAndAny() {
    any x = 4;
    any|error y = x;
    assertEquality(4, y);
}

const ASSERTION_ERROR_REASON = "AssertionError";
type AssertionError error;

function assertEquality(any|error expected, any|error actual) {
    if expected is anydata && actual is anydata && expected == actual {
        return;
    }

    if expected === actual {
        return;
    }

    panic AssertionError(ASSERTION_ERROR_REASON, message = "expected '" + expected.toString() + "', found '" + actual.toString () + "'");
}

function assertTrue(any|error actual) {
    assertEquality(true, actual);
}
