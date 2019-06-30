import ballerina/test;
import ballerina/io;

# Before Suite Function

@test:BeforeSuite
function beforeSuiteServiceFunc () {
    io:println("I'm the before suite service function!");
}

# Test function

@test:Config
function testServiceFunction () {
    io:println("Do your service Tests!");
    test:assertTrue(true , msg = "Failed!");
}

# After Suite Function

@test:AfterSuite
function afterSuiteServiceFunc () {
    io:println("I'm the after suite service function!");
}