import ballerina/test;
import ballerina/io;

// This test function depends on `testFunction3`.
@test:Config {
    // You can provide a list of depends on functions here.
    dependsOn: ["testFunction3"]
}
function testFunction1() {
    io:println("I'm in test function 1!");
    test:assertTrue(true, msg = "Failed!");
}

// This test function depends on `testFunction1`.
@test:Config {
    dependsOn: ["testFunction1"]
}
function testFunction2() {
    io:println("I'm in test function 2!");
    test:assertTrue(true, msg = "Failed!");
}

// This is a rondom test function, this will randomly execute without depending on other functions.
// But note that other function do depend on this.
@test:Config
function testFunction3() {
    io:println("I'm in test function 3!");
    test:assertTrue(true, msg = "Failed!");
}