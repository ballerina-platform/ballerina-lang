import ballerina/test;
import ballerina/io;
import manny/mod2;

# Test function

@test:Config
function testFunction () {
    io:println("Hello Manny !! I got a message --> " + mod2:add());
    test:assertTrue(true , msg = "Failed!");
}

