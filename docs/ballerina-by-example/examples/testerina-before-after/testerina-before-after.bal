import ballerina/test;
import ballerina/io;

// Before test function
function beforeFunc () {
    io:println("I'm the before function!");
}

// Test function
@test:Config{
    before:"beforeFunc",
    after:"afterFunc"
}
function testFunction () {
    io:println("I'm in test function!");
    test:assertTrue(true , msg = "Failed!");
}

// After test function
function afterFunc () {
    io:println("I'm the after function!");
}
