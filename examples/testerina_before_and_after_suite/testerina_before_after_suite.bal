import ballerina/test;
import ballerina/io;

// The `BeforeSuite` function is executed before all test functions in this package. 
@test:BeforeSuite
function beforeFunc() {
    io:println("I'm the before suite function!");
}

// Test function.
@test:Config
function testFunction1() {
    io:println("I'm in test function 1!");
    test:assertTrue(true, msg = "Failed");
}

// Test function.
@test:Config
function testFunction2() {
    io:println("I'm in test function 2!");
    test:assertTrue(true, msg = "Failed");
}

// The `AfterSuite` function is executed after all the test functions in this package. 
@test:AfterSuite
function afterFunc() {
    io:println("I'm the after suite function!");
}
