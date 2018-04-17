package a.b;

import ballerina/io;

function main (string... args) returns (int) {
    io:println("Hello, World! " + args[0]);
    
    return 0;
}
