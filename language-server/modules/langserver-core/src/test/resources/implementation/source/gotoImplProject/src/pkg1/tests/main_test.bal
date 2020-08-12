import ballerina/test;
import ballerina/io;

type TestObject3 object {
    function testOb3Signature1();

    function testOb3Function1() {
        
    }
};

function TestObject3.testOb3Signature1() {
    // Function implementation within test sources
}

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

@test:Config{
    before:"beforeFunc",
    after:"afterFunc"
}
function testFunction () {
    io:println("I'm in test function!");
    test:assertTrue(true , msg = "Failed!");
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
