import ballerina/test;
import ballerina/io;

int x = 8;


# Before Suite Function
@test:BeforeSuite
function beforeSuiteFunc () {
    io:println("I'm the before suite function!");
}

# Before test function

function beforeFunc () {
    io:println("I'm the before function!");
}

# Test function

@test:Config {
    before:"beforeFunc",
    after:"afterFunc"
}
function testFunction () {
    io:println("I'm in test function!");
    test:assertTrue(false , msg = "Something Failed!");
    test:assertTrue(x == 8, msg = "Test init is not working");
    test:assertTrue(y == 9, msg = "Module init is not working");
}

# After test function

function afterFunc () {
    io:println("I'm the after function!");
}

# After Suite Function

@test:AfterSuite {}
function afterSuiteFunc () {
    io:println("I'm the after suite function!");
}
