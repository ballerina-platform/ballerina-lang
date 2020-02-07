import ballerina/test;

@test:Config{
    enable:false
}
function testFunc1 () {
    test:assertTrue(false, msg = "errorMessage");
}

@test:Config{
    enable:true
}
function testFunc4 () {
    test:assertTrue(true, msg = "errorMessage");
}

// test without enable attribute
@test:Config{
}
function testFunc6 () {
    test:assertFalse(false, msg = "errorMessage");
}
