import ballerina/io;
import ballerina/test;
import wso2test/baz;

# Before Suite Function

string testStr1 = baz:bazStr1;
string testStr2 = baz:bazFn();

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
    io:println("I'm in test function!");
    test:assertTrue(true, msg = "Failed!");
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
