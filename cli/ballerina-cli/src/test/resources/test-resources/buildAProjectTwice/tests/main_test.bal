import ballerina/test;

// Before Suite Function

@test:BeforeSuite
function beforeSuiteFunc() {
}

// Test function

@test:Config {}
function testFunction() {
    test:assertTrue(true, msg = "Failed!");
}

// After Suite Function

@test:AfterSuite
function afterSuiteFunc() {
}
