import ballerina/io;

public function main (string... args) returns (int) {
    io:println("Hello, World! " + args[0]);
    
    return 0;
}
