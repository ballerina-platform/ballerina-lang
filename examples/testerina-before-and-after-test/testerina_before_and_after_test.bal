import ballerina/io;
import ballerina/test;

// This before-function is executed before the test function.
function beforeFunc() {
    io:println("I'm the before function!");
}

// The Test function.
// Use the `before` and `after` attributes to define the function names
// of the functions that need to be executed before and after the test function.
@test:Config {
    before: "beforeFunc",
    after: "afterFunc"
}
function testFunction() {
    io:println("I'm in test function!");
    test:assertTrue(true, msg = "Failed!");
}

// This after-function is executed after the test function.
function afterFunc() {
    io:println("I'm the after function!");
}
