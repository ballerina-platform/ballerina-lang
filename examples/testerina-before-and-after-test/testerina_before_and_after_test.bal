import ballerina/test;
import ballerina/io;

// Before function is executed before the test function
function beforeFunc() {
    io:println("I'm the before function!");
}

// Test function
//Use the `before` and `after` attributes to define the function names
//of the functions that need to executed before and after the test function.
@test:Config {
    before: "beforeFunc",
    after: "afterFunc"
}
function testFunction() {
    io:println("I'm in test function!");
    test:assertTrue(true, msg = "Failed!");
}

// After function is executed after the test function
function afterFunc() {
    io:println("I'm the after function!");
}
