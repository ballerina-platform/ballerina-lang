import ballerina.test;

// ===== Assert Equals ===== //

// Compare Integer values
@test:config
function testAssertIntEquals (){

    int answer = 0;
    int a = 5;
    int b = 3;
    answer = intAdd(a, b);
    test:assertEquals(answer, 8, "intAdd function failed");

}

// Compare Float values
@test:config
function testAssertFloatEquals () {
    float a = 10.000;
    float b = 20.050;
    float answer = floatAdd(a, b);
    test:assertEquals(answer, 30.050, "floatAdd function failed");
}

// Compare String values
@test:config
function testAssertStringEquals () {
    string a = "John";
    string b = "Doe";
    string concatenated = stringConcat(a, b);
    test:assertEquals(concatenated, "JohnDoe", "string concatenation failed");
}

// Compare Json objects
@test:config
function testAssertJsonEquals () {

    json a = { "name": "Ballerina" };
    json b = { "name": "Ballerina" };
    test:assertEquals(a, b, "json assert equals failed");
}

// Compare boolean values
@test:config
function testAssertBooleanEquals () {

    boolean x = true;
    boolean y = true;
    test:assertEquals(x, y, "assertBooleanEquals failed");
}

// Compare string arrays
@test:config
function testAssertStringArrayEquals () {

    string[] x = ["A", "B", "C"];
    string[] y = ["A", "B", "C"];

    test:assertEquals(x, y, "String arrays are not equal");

}

// Compare Integer arrays
@test:config
function testAssertIntArrayEquals () {

    int[] x = [1, 2, 3];
    int[] y = [1, 2, 3];

    test:assertEquals(x, y, "Int arrays are not equal");
}

// Compare Float arrays
@test:config
function testAssertFloatArrayEquals () {

    float[] x = [1.1, 2.2, 3.3];
    float[] y = [1.1, 2.2, 3.3];

    test:assertEquals(x, y, "failed");
}

// ===== Assert Not Equals ==== //

// Compare distinct strings
@test:config
function testAssertNotEqualsString () {

    string s1 = "abc";
    string s2 = "def";

    test:assertNotEquals(s1, s2, "Strings are equal");

}

// Compare distinct Json
@test:config
function testAssertNotEqualsJson () {

    json s1 = {"a":"b"};
    json s2 = {"a":"c"};
    test:assertNotEquals(s1, s2, "Json are equal");
}


// ===== Assert True ===== //

// Asserts true
@test:config
function testAssertTrue () {
    boolean value = true;
    test:assertTrue(value, "assertTrue failed");
}

// Assert false
@test:config
function testAssertFalse () {

    boolean value = false;
    test:assertFalse(value, "assertFalse failed");
}

// ==== Asset Fail ==== //

// Assert Fail
@test:config
function testAssertFail1 () {
    try {
        // I'm expecting a error
        error err;
        throw err;
        test:assertFail("Exception Never occured");

    } catch (error e) {
        // Do more assertions
    }
}

// Assert Fail
@test:config
function testAssertFail2 () {
    if (true) {
        return;
    }
    test:assertFail("assertFailed");
}


// Test functions
function intAdd (int a, int b) (int) {
    return a + b;
}

function floatAdd (float a, float b) (float) {
    return a + b;
}

function stringConcat (string a, string b) (string) {
    return a + b;
}
