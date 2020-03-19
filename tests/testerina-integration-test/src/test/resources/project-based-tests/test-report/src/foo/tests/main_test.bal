import ballerina/io;
import ballerina/test;

# Test function

@test:Config {}
function testMain() {
    io:println("I'm calling the main function in source file!");
    main();
    test:assertTrue(false, msg = "Failed!");
}

@test:Config {
    dependsOn: ["testMain"]
}
function testFunction() {
    io:println("I'm in test function!");
    test:assertTrue(true, msg = "Failed!");
}

