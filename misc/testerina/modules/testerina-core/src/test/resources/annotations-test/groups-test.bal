import ballerina.test;

@test:config{
    groups:["g1","g2"]
}
function testFunc1 () {
    test:assertFalse(false, "errorMessage");
}

@test:config{
    groups:["g1","g2","g3"]
}
function testFunc2 () {
    test:assertFalse(false, "errorMessage");
}

@test:config{
    groups:["g1","g2","g3","g4"]
}
function testFunc3 () {
    test:assertFalse(false, "errorMessage");
}

@test:config{
    groups:["g5"]
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
