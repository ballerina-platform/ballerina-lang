import ballerina/io;
import ballerina/test;
import ballerina/java;

public function getString() returns handle = @java:Method {
    class:"org.wso2.test.StaticMethods"
} external;

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
    string result =  <string>java:toString(getString());
    test:assertEquals(result, "This is a test string value !!!", msg = "Interop method call failed!");
    io:println("Tested utils getString method using interop and received: " + result);
}

# After test function

function afterFunc() {
    io:println("I'm the after function!");
}

# After Suite Function

@test:AfterSuite {}
function afterSuiteFunc() {
    io:println("I'm the after suite function!");
}
