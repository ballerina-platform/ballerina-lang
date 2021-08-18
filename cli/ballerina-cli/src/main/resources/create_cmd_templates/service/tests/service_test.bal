import ballerina/io;
import ballerina/http;
import ballerina/test;

http:Client testClient = check new ("http://localhost:9090/hello");

// Before Suite Function

@test:BeforeSuite
function beforeSuiteFunc() {
    io:println("I'm the before suite function!");
}

// Test function

@test:Config {}
function testServiceWithProperName() {
    string|error response = testClient->get("/sayHello/?name=John");
    test:assertEquals(response, "Hello, John");
}

// Negative test function

@test:Config {}
function testServiceWithEmptyName() returns error? {
    http:Response response = check testClient->get("/sayHello/?name=");
    test:assertEquals(response.getTextPayload(), "name should not be empty!");
}

// After Suite Function

@test:AfterSuite
function afterSuiteFunc() {
    io:println("I'm the after suite function!");
}
