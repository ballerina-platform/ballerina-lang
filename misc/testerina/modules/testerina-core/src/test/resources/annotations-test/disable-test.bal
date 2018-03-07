import ballerina.test;

@test:config{
    enable:false
}
function testFunc1 () {
    test:assertTrue(false, "errorMessage");
}

@test:config{
    enable:true
}
function testFunc4 () {
    test:assertTrue(true, "errorMessage");
}

// test without enable attribute
@test:config{
}
function testFunc6 () {
    test:assertFalse(false, "errorMessage");
}
