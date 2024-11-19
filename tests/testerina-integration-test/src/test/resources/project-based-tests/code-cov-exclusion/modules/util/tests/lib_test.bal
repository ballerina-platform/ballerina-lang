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

@test:Config {}
function testIntAdd() {
    test:assertEquals(intAdd(10, 3), 13);
}

// After Suite Function

@test:AfterSuite
function afterSuiteFunc() {

}
