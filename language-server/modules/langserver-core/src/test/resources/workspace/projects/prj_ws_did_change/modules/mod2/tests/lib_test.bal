
import ballerina/test;

// Before Suite Function

@test:BeforeSuite
function beforeSuiteFunc() {
    
}

// Test function

@test:Config {}
function testFunction() {
    string name = "John";
    string welcomeMsg = hello(name);
    test:assertEquals("Hello, John", welcomeMsg);
}

// Negative Test function

@test:Config {}
function negativeTestFunction() {
    string name = "";
    string welcomeMsg = hello(name);
    test:assertEquals("Hello, World!", welcomeMsg);
}

// After Suite Function

@test:AfterSuite
function afterSuiteFunc() {
    
}
