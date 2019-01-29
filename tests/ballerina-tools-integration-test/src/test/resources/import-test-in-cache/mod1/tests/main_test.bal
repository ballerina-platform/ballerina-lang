import ballerina/test;
import ballerina/io;
import fanny/mod2;

# Test function

@test:Config
function testFunction () {
    io:println("Hello!! I got a message from a cached repository module --> " + mod2:add());
    test:assertTrue(true , msg = "Failed!");
}

