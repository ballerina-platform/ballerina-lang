import ballerina/test;

@test:Config{
    enable:false
}
function testDisableFunc1 () {
    test:assertTrue(false, msg = "this test is not expected to run");
}

@test:Config{
    enable:true
}
function testDisableFunc2 () {
    test:assertTrue(true);
}

// test without enable attribute
@test:Config{
}
function testDisableFunc3 () {
    test:assertFalse(false);
}
