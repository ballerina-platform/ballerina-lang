import ballerina/io;
import ballerina/test;

# Prints `Hello World`.

public function main() {
    io:println("Hello World!");
}

// Check if this test gets executed
@test:Config {}
function outsideTestFunction() {
    io:println("I'm in outside test function!");
    test:assertTrue(true, msg = "Failed!");
}
