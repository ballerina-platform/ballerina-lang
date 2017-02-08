import ballerina.lang.system;

// This will be a separate .bal file containing core functions.
// TODO: ballerinalang multiple bal file support needed to split files
function addTwoNumbers(int a, int b) (int) {
    return (a + b);
}

// This will be a separate _test.bal file containing all test functions.
// TODO: ballerinalang multiple bal file support needed to split files
function testPositive() {
    assertTrue(addTwoNumbers(1, 2) == 2);
    assertEquals(addTwoNumbers(1, 2), 3, "Function failure");
}

function testNegative() {
    assertTrue(addTwoNumbers(-1, -2) == 3, "Assert Failed for Negative test");
}

function testZero() {
    assertTrue(addTwoNumbers(0, 0) == 1);
}

// This will be a separate .bal file containing assert logic
function assertTrue(boolean condition) {
    // TODO: once ballerinalang supports null values, pass null here
    assertTrue(condition, "");
}

function assertTrue(boolean condition, string message) {
    if (!condition) {
        // TODO: once ballerinalang supports null values, do the check here
        if (message == "") {
            message = "Assert Failed";
        }
        // TODO: once ballerinalang supports BallerinaExceptions, throw it here
        system:println(message);
    }
}

function assertFalse(boolean condition) {
    // TODO: once ballerinalang supports null values, pass null here
    assertFalse(condition, "");
}

function assertFalse(boolean condition, string message) {
    if (condition) {
        // TODO: once ballerinalang supports null values, do the check here
        if (message == "") {
            message = "Assert Failed";
        }
        // TODO: once ballerinalang supports BallerinaExceptions, throw it here
        system:println(message);
    }
}

function assertEquals(string actual, string expected) {
    // TODO: once ballerinalang supports null values, pass null here
    assertEquals(actual, expected, "");
}

function assertEquals(string actual, string expected, string message) {
    // Since string is a primitive type in ballerina
    if (actual != expected) {
        // TODO: once ballerinalang supports null values, do the check here
        if (message == "") {
            message = "String not equal: expected: " + expected + " and actual: "+ actual;
        }
        // TODO: once ballerinalang supports BallerinaExceptions, throw it here
        system:println(message);
    }
}

function assertEquals(int actual, int expected) {
    // TODO: once ballerinalang supports null values, pass null here
    assertEquals(actual, expected, "");
}

function assertEquals(int actual, int expected, string message) {
    if (actual != expected) {
        // TODO: once ballerinalang supports null values, do the check here
        if (message == "") {
            message = "Integer not equal: expected: " + expected + " and actual: "+ actual;
        }
        // TODO: once ballerinalang supports BallerinaExceptions, throw it here
        system:println(message);
    }
}

// https://github.com/wso2/ballerina/issues/1564
function assertEquals(double actual, double expected) {
    // TODO: once ballerinalang supports null values, pass null here
    assertEquals(actual, expected, "");
}

function assertEquals(double actual, double expected, string message) {
    if (actual != expected) {
        // TODO: once ballerinalang supports null values, do the check here
        if (message == "") {
            message = "Double not equal: expected: " + expected + " and actual: "+ actual;
        }
        // TODO: once ballerinalang supports BallerinaExceptions, throw it here
        system:println(message);
    }
}

// https://github.com/wso2/ballerina/issues/1613
function assertEquals(boolean actual, boolean expected) {
    // TODO: once ballerinalang supports null values, pass null here
    assertEquals(actual, expected, "");
}

function assertEquals(boolean actual, boolean expected, string message) {
    if (actual != expected) {
        // TODO: once ballerinalang supports null values, do the check here
        if (message == "") {
            message = "Boolean not equal: expected: " + expected + " and actual: "+ actual;
        }
        // TODO: once ballerinalang supports BallerinaExceptions, throw it here
        system:println(message);
    }
}