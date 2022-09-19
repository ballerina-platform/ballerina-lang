import ballerina/lang.value;

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

public type CustomError error<record {|value:Cloneable failedAttempts;|}>;

public client class Client {
    remote function foo() returns [int, int] {
        return [0, 0];
    }

    remote function foo1() returns record { string a; } {
        return { a: "a" };
    }

    remote function foo2() returns error {
        return error("the error reason");
    }

    remote function foo3() returns CustomError {
        return error("foo3 error", failedAttempts = 3);
    }
}

public function restActionResultAssignment() returns [int, int, string, string, string, value:Cloneable] {
    Client c = new();
    var [a, b] = c->foo();
    var { a: d } = c->foo1();
    var error(r) = c->foo2();
    var error(r2, failedAttempts = failedAttempts) = c->foo3();
    return [a, b, d, r, r2, failedAttempts];
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

type Topt1 record {
    int x?;
    int y?;
};

function testOptionalFieldAssignment1() {
    Topt1 t = {x: 2, y: 4};
    t.x = ();
    assertEquality(t.x, ());
    assertEquality(t.y, 4);
    t.y = ();
    assertEquality(t.x, ());
    assertEquality(t.y, ());
}

type Topt2 record {
    int a;
    record {
        int b?;
    }[1] c;
};

function testOptionalFieldAssignment2() {
    Topt2 t = {a: 2, c: [{b: 4}]};
    (t.c)[0].b = ();
    assertEquality(t.a, 2);
    assertEquality((t.c)[0].b, ());
}

type Topt3 record {
    int x?;
    int? y?;
};

function testOptionalFieldAssignment3() {
    Topt3 t3 = {x: 2, y: 4};
    t3.y = ();
    assertEquality(t3.toString(), "{\"x\":2,\"y\":null}");
    Topt1 t1 = {x: 21, y: 41};
    t1.y = ();
    assertEquality(t1.toString(), "{\"x\":21}");
}

const ASSERTION_ERROR_REASON = "AssertionError";

function assertEquality(any|error expected, any|error actual) {
    if expected is anydata && actual is anydata && expected == actual {
        return;
    }

    if expected === actual {
        return;
    }

    string expectedValAsString = expected is error ? expected.toString() : expected.toString();
    string actualValAsString = actual is error ? actual.toString() : actual.toString();
    panic error(ASSERTION_ERROR_REASON,
                            message = "expected '" + expectedValAsString + "', found '" + actualValAsString + "'");
}

function assertTrue(any|error actual) {
    assertEquality(true, actual);
}
