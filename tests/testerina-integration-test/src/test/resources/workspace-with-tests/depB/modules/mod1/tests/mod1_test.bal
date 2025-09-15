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
    test:assertEquals(welcomeMsg, "Hello, Mary");
}

// Negative Test function

@test:Config {}
function negativeTestFunction() {
    string welcomeMsg = hello(());
    test:assertEquals(welcomeMsg, "Hello, World!");
}

// After Suite Function

@test:AfterSuite
function afterSuiteFunc() {
}
