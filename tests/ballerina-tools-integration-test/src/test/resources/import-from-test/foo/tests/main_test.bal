import ballerina/test;
import ballerina/io;
import bar;

# Test function

@test:Config
function testFunction () {
    string msg = "Addition is == " + bar:add();
    io:println(msg);
}

