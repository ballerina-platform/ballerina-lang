import ballerina/test;
import ballerina/io;

function beforeFunc2() {
    io:println("I'm the before function2!");
}

@test:Config {
    before: "beforeFunc2",
    after: "afterFunc2"
}
function testFunction2() {
    io:println("I'm in test function2!");

    boolean[] booleanValues = [true, false];

    foreach boolVal in booleanValues {
        sourceFunc2(boolVal);
    }

    test:assertTrue(true, msg = "Failed2!");
}

function afterFunc2() {
    io:println("I'm the after function2!");
}
