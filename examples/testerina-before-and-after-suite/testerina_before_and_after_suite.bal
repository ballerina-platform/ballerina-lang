import ballerina/io;
import ballerina/test;

// The function annotated with `BeforeSuite` is executed before all the test functions in the module.
@test:BeforeSuite
function beforeSuit() {
    io:println("I'm the before suite function!");
}

// A Test function.
@test:Config {}
function testFunction1() {
    io:println("I'm in test function 1!");
    test:assertTrue(true, msg = "Failed");
}

// A Test function.
@test:Config {}
function testFunction2() {
    io:println("I'm in test function 2!");
    test:assertTrue(true, msg = "Failed");
}

// The function annotated with `AfterSuite` will be executed after all the test functions in the module have executed.
@test:AfterSuite
function afterSuite() {
    io:println("I'm the after suite function!");
}
