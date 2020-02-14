import ballerina/test;

// The `assertEquals()` function allows you to compare primitive types (e.g., int) against composite objects.
// Compares values of the type `int`.
@test:Config {}
function testAssertIntEquals() {
    int answer = 0;
    int a = 5;
    int b = 3;
    answer = intAdd(a, b);
    test:assertEquals(answer, 8, msg = "int values not equal");
}

// Compares values of the type `float`.
@test:Config {}
function testAssertFloatEquals() {
    float a = 10.000;
    float b = 20.050;
    float answer = floatAdd(a, b);
    test:assertEquals(answer, 30.050, msg = "float values not equal");
}

// Compares values of the type `string`.
@test:Config {}
function testAssertStringEquals() {
    string a = "John";
    string b = "Doe";
    string concatenated = stringConcat(a, b);
    test:assertEquals(concatenated, "JohnDoe", msg = "string values not equal");
}

// Compares values of the type `json`.
@test:Config {}
function testAssertJsonEquals() {
    json a = {"name": "Ballerina"};
    json b = {"name": "Ballerina"};
    test:assertEquals(a, b, msg = "JSON values not equal");
}

// Compares values of the type `boolean`.
@test:Config {}
function testAssertBooleanEquals() {
    boolean x = true;
    boolean y = true;
    test:assertEquals(x, y, msg = "boolean values not equal");
}

// Compares values of the type `string[]`.
@test:Config {}
function testAssertStringArrayEquals() {
    string[] x = ["A", "B", "C"];
    string[] y = ["A", "B", "C"];
    test:assertEquals(x, y, msg = "string array values not equal");
}

// Compares values of the type `int[]`.
@test:Config {}
function testAssertIntArrayEquals() {
    int[] x = [1, 2, 3];
    int[] y = [1, 2, 3];
    test:assertEquals(x, y, msg = "int array values not equal");
}

// Compares values of the type `float[]`.
@test:Config {}
function testAssertFloatArrayEquals() {
    float[] x = [1.1, 2.2, 3.3];
    float[] y = [1.1, 2.2, 3.3];
    test:assertEquals(x, y, msg = "float array values not equal");
}

// Compares distinct values of the type `string`.
@test:Config {}
function testAssertNotEqualsString() {
    string s1 = "abc";
    string s2 = "def";
    test:assertNotEquals(s1, s2, msg = "string values are equal");
}

// Compares distinct values of the type `json`.
@test:Config {}
function testAssertNotEqualsJson() {
    json s1 = {"a": "b"};
    json s2 = {"a": "c"};
    test:assertNotEquals(s1, s2, msg = "JSON values are equal");
}

// Asserts `true`.
@test:Config {}
function testAssertTrue() {
    boolean value = true;
    test:assertTrue(value, msg = "AssertTrue failed");
}

// Asserts `false`.
@test:Config {}
function testAssertFalse() {
    boolean value = false;
    test:assertFalse(value, msg = "AssertFalse failed");
}

// A test-example, which is failing intentionally.
@test:Config {}
function testAssertFail() {
    if (true) {
        return;
    }
    test:assertFail(msg = "AssertFailed");
}

function intAdd(int a, int b) returns (int) {
    return (a + b);
}

function floatAdd(float a, float b) returns (float) {
    return (a + b);
}

function stringConcat(string a, string b) returns (string) {
    return (a + b);
}
