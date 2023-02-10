// import ballerina/io;
import ballerina/test;

// Before Suite Function

@test:BeforeSuite
function beforeSuiteFunc() {
    // io:println("I'm the before suite function!");
}

// Test function

@test:Config {}
function testFunction() {
    string name = "John";
    string welcomeMsg = hellos(name);
    test:assertEquals("Hello, John", welcomeMsg);
}

// Negative Test function

@test:Config {}
function negativeTestFunction() {
    string name = "";
    string welcomeMsg = hellos(name);
    test:assertEquals("Hello, World!", welcomeMsg);
}

// After Suite Function

@test:AfterSuite
function afterSuiteFunc() {
    // io:println("I'm the after suite function!");
}
