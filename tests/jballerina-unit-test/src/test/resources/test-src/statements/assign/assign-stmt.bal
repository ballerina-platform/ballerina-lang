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

function testAssignAnyToUnionWithErrorAndAny() {
    any x = 4;
    any|error y = x;
    assertEquality(4, y);
}

function testAssignVarInQueryExpression() {
    xml x1 = xml `<book><a>The Lost World</a><b>Clean Code</b></book>`;

    var x2 = from xml element in x1 select element;
    assertTrue(x2 is xml);
    xml x3 = x2;
    assertEquality(x3, xml `<book><a>The Lost World</a><b>Clean Code</b></book>`);

    var x4 = from xml element in x1/<a> select element;
    assertTrue(x4 is xml<xml:Element>);
    xml x5 = x4;
    assertEquality(x5, xml `<a>The Lost World</a>`);

    var x6 = "string";

    var x7 = from string element in x6 select element;
    assertTrue(x7 is string);
    string x8 = x7;
    assertEquality(x8, "string");

    var x9 = from string element in x6 select "a";
    assertTrue(x9 is string);
    string x10 = x9;
    assertEquality(x10, "aaaaaa");

    var x11 = [1, 2];
    var x12 = [x1, x1];

    var x13 = from int element in x11 select element;
    assertTrue(x13 is int[]);
    int[] x14 = x13;
    assertEquality(x14, <int[]>[1, 2]);

    var x15 = from int element in x11 select 1;
    assertTrue(x15 is int[]);
    int[] x16 = x15;
    assertEquality(x16, <int[]>[1, 1]);

    var x17 = from int element in x11 select string `string ${element}`;
    assertTrue(x17 is string[]);
    string[] x18 = x17;
    assertEquality(x18, <string[]>["string 1", "string 2"]);

    var x19 = from xml element in x12 select element;
    assertTrue(x19 is xml[]);
    xml[] x20 = x19;
    assertEquality(x20, <xml[]>[x1, x1]);

    var x21 = [1, "string 1", true, x1];

    var x22 = from var element in x21 select element;
    assertTrue(x22 is (int|string|boolean|xml)[]);
    (int|string|boolean|xml)[] x23 = x22;
    assertEquality(x23, x21);

    var x24 = from int element in 1...4 select element;
    assertTrue(x24 is int[]);
    int[] x25 = x24;
    assertEquality(x25, <int[]>[1, 2, 3, 4]);
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
