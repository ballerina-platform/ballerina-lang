import ballerina/test;
import ballerina/io;

// Before each function, which is executed before each test function
@test:BeforeEach
function beforeFunc() {
    io:println("I'm the before function!");
}

// Test function
@test:Config
function testFunction1() {
    io:println("I'm in test function 1!");
    test:assertTrue(true, msg = "Failed!");
}

// Test function
@test:Config
function testFunction2() {
    io:println("I'm in test function 2!");
    test:assertTrue(true, msg = "Failed!");
}

// Test function
@test:Config
function testFunction3() {
    io:println("I'm in test function 3!");
    test:assertTrue(true, msg = "Failed!");
}
