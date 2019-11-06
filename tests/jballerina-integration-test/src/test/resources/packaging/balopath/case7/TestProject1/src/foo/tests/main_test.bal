import ballerina/io;
import ballerina/test;
import wso2/utils;
import ballerinax/java;

function testAcceptNothingButReturnString() returns handle {
    return utils:getString();
}

# Before Suite Function

@test:BeforeSuite
function beforeSuiteFunc() {
    io:println("I'm the before suite function!");
}

# Before test function

function beforeFunc() {
    io:println("I'm the before function!");
}

# Test function

@test:Config {
    before: "beforeFunc",
    after: "afterFunc"
}
function testFunction() {
    string result =  <string>java:toString(testAcceptNothingButReturnString());
    test:assertEquals(result, "This is a test string value !!!", msg = "Interop method call failed!");
    io:println("Tested utils getString method using interop and received: " + result);
}

# After test function

function afterFunc() {
    io:println("I'm the after function!");
}

# After Suite Function

@test:AfterSuite
function afterSuiteFunc() {
    io:println("I'm the after suite function!");
}
