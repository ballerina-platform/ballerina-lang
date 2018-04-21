import ballerina/test;

// ===== Assert Equals ===== //

// Compare Integer values
@test:Config
function testAssertIntEquals (){

    int answer = 0;
    int a = 5;
    int b = 3;
    answer = intAdd(a, b);
    test:assertEquals(answer, 8, msg = "intAdd function failed");

}

// Compare Float values
@test:Config
function testAssertFloatEquals () {
    float a = 10.000;
    float b = 20.050;
    float answer = floatAdd(a, b);
    test:assertEquals(answer, 30.050, msg = "floatAdd function failed");
}

// Compare String values
@test:Config
function testAssertStringEquals () {
    string a = "John";
    string b = "Doe";
    string concatenated = stringConcat(a, b);
    test:assertEquals(concatenated, "JohnDoe", msg = "string concatenation failed");
}

// Compare Json objects
@test:Config
function testAssertJsonEquals () {

    json a = { "name": "Ballerina" };
    json b = { "name": "Ballerina" };
    test:assertEquals(a, b, msg = "json assert equals failed");
}

// Compare boolean values
@test:Config
function testAssertBooleanEquals () {

    boolean x = true;
    boolean y = true;
    test:assertEquals(x, y, msg = "assertBooleanEquals failed");
}

// Compare string arrays
@test:Config
function testAssertStringArrayEquals () {

    string[] x = ["A", "B", "C"];
    string[] y = ["A", "B", "C"];

    test:assertEquals(x, y, msg = "String arrays are not equal");

}

// Compare Integer arrays
@test:Config
function testAssertIntArrayEquals () {

    int[] x = [1, 2, 3];
    int[] y = [1, 2, 3];

    test:assertEquals(x, y, msg = "Int arrays are not equal");
}

// Compare Float arrays
@test:Config
function testAssertFloatArrayEquals () {

    float[] x = [1.1, 2.2, 3.3];
    float[] y = [1.1, 2.2, 3.3];

    test:assertEquals(x, y, msg = "failed");
}

// ===== Assert Not Equals ==== //

// Compare distinct strings
@test:Config
function testAssertNotEqualsString () {

    string s1 = "abc";
    string s2 = "def";

    test:assertNotEquals(s1, s2, msg = "Strings are equal");

}

// Compare distinct Json
@test:Config
function testAssertNotEqualsJson () {

    json s1 = {"a":"b"};
    json s2 = {"a":"c"};
    test:assertNotEquals(s1, s2, msg = "Json are equal");
}


// ===== Assert True ===== //

// Asserts true
@test:Config
function testAssertTrue () {
    boolean value = true;
    test:assertTrue(value, msg = "assertTrue failed");
}

// Assert false
@test:Config
function testAssertFalse () {

    boolean value = false;
    test:assertFalse(value, msg = "assertFalse failed");
}

// ==== Asset Fail ==== //

// Assert Fail
@test:Config
function testAssertFail1 () {
    try {
        // I'm expecting a error
        error err = {};
        test:assertFail(msg = "Exception Never occured");

    } catch (error e) {
        // Do more assertions
    }
}

// Assert Fail
@test:Config
function testAssertFail2 () {
    if (true) {
        return;
    }
    test:assertFail(msg = "assertFailed");
}


// Test functions
function intAdd (int a, int b) returns (int) {
    return (a + b);
}

function floatAdd (float a, float b) returns (float) {
    return (a + b);
}

function stringConcat (string a, string b) returns (string) {
    return (a + b);
}
