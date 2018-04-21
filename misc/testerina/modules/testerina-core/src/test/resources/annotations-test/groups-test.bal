import ballerina/test;

@test:Config{
    groups:["g1","g2"]
}
function testFunc1 () {
    test:assertFalse(false, msg = "errorMessage");
}

@test:Config{
    groups:["g1","g2","g3"]
}
function testFunc2 () {
    test:assertFalse(false, msg = "errorMessage");
}

@test:Config{
    groups:["g1","g2","g3","g4"]
}
function testFunc3 () {
    test:assertFalse(false, msg = "errorMessage");
}

@test:Config{
    groups:["g5"]
}
function testFunc4 () {
    test:assertFalse(true, msg = "errorMessage");
}

@test:Config{}
function testFunc5 () {
    test:assertFalse(false, msg = "errorMessage");
}
