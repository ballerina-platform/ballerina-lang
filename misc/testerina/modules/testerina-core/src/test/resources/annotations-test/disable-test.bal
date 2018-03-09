import ballerina.test;

@test:config{
    disabled:true
}
function testFunc1 () {
    test:assertFalse(false, "errorMessage");
}

@test:config{
    disabled:false
}
function testFunc3 () {
    test:assertFalse(false, "errorMessage");
}

@test:config{
    disabled:true
}
function testFunc4 () {
    test:assertFalse(true, "errorMessage");
}

@test:config{}
function testFunc5 () {
    test:assertFalse(false, "errorMessage");
}

//Function without annotations
function testFunc6 () {
    test:assertFalse(false, "errorMessage");
}
