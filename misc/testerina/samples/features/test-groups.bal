import ballerina/test;


// You can add your tests to a particular group
// After adding to a group the execution can be controlled with runtime flags.
@test:Config {
     groups: ["strings"]
}
function testAssertFloatEquals () {
    float a = 10.000;
    float b = 20.050;
    float answer = floatAdd(a, b);
    test:assertEquals(answer, 30.050, msg = "floatAdd function failed");
}

// Compare String values
@test:Config {
    groups: ["strings"]
}
function testAssertStringEquals () {
    string a = "John";
    string b = "Doe";
    string concatenated = stringConcat(a, b);
    test:assertEquals(concatenated, "JohnDoe", msg = "string concatenation failed");
}


// Compare string arrays
@test:Config {
    groups: ["arrays"]
}
function testAssertStringArrayEquals () {

    string[] x = ["A", "B", "C"];
    string[] y = ["A", "B", "C"];

    test:assertEquals(x, y, msg = "String arrays are not equal");

}

// Compare Integer arrays
@test:Config {
    groups: ["arrays"]
}
function testAssertIntArrayEquals () {

    int[] x = [1, 2, 3];
    int[] y = [1, 2, 3];

    test:assertEquals(x, y, msg = "Int arrays are not equal");
}

// Compare distinct strings
@test:Config {
    groups: ["negative-tests"]
}
function testAssertNotEqualsString () {

    string s1 = "abc";
    string s2 = "def";

    test:assertNotEquals(s1, s2, msg = "Strings are equal");

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
