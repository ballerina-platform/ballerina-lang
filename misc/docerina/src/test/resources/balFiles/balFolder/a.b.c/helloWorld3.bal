
import ballerina/io;

function abc (string args) returns (int) {
    io:println("Hello, World! "+args);
    
    return 0;
}
