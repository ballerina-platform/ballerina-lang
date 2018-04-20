
import ballerina/io;

function xyz (string args) returns (int) {
    io:println("Hello, World! "+args);
    
    return 0;
}
