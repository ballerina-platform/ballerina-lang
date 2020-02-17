import ballerina/test;

// This test should not run
@test:Config{
    enable:false
}
function testDisableFunc1 () {
    test:assertTrue(false, msg = "this test is not expected to run");
}

// This test should run
@test:Config{
    enable:true
}
function testDisableFunc2 () {
    test:assertTrue(true);
}

// Test without enable attribute. This should run.
@test:Config{
}
function testDisableFunc3 () {
    test:assertFalse(false);
}
