import ballerina/test;
import ballerina/io;
import mod2;

# Test function

@test:Config
function testFunction () {
    io:println("Hello!! I got a message from an unknown person --> " + mod2:add());
    test:assertTrue(true , msg = "Failed!");
}

