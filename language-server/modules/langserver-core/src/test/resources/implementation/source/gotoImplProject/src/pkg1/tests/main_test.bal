import ballerina/test;
import ballerina/iox;

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
    iox:println("I'm the before suite function!");
}

# Before test function

function beforeFunc () {
    iox:println("I'm the before function!");
}

# Test function

@test:Config{
    before:"beforeFunc",
    after:"afterFunc"
}
function testFunction () {
    iox:println("I'm in test function!");
    test:assertTrue(true , msg = "Failed!");
}

# After test function

function afterFunc () {
    iox:println("I'm the after function!");
}

# After Suite Function

@test:AfterSuite {}
function afterSuiteFunc () {
    iox:println("I'm the after suite function!");
}
