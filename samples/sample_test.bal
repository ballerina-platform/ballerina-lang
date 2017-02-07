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
}

function testNegative() {
    assertTrue(addTwoNumbers(-1, -2) == 3, "Assert Failed for Negative test");
}

function testZero() {
    assertTrue(addTwoNumbers(0, 0) == 1);
}

// This will be a separate .bal file containing assert logic
function assertTrue(boolean condition) {
    assertTrue(condition, "");
}

function assertTrue(boolean condition, string message) {
    if (!condition) {
        if (message == "") {
            message = "Assert Failed";
        }
        // TODO: once ballerinalang supports BallerinaExceptions, throw it here
        system:println(message);
    }
}
