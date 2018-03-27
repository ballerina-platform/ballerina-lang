import ballerina/test;
import ballerina/io;

// Before suite function
@test:BeforeSuite
function beforeFunc () {
    io:println("I'm the before suite function!");
}

// Test function
@test:Config
function testFunction1 () {
    io:println("I'm in test function 1!");
    test:assertTrue(true , msg = "Failed");
}

// Test function
@test:Config
function testFunction2 () {
    io:println("I'm in test function 2!");
    test:assertTrue(true , msg = "Failed");
}

// After suite function
@test:AfterSuite
function afterFunc () {
    io:println("I'm the after suite function!");
}
