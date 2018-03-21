package a.b.c;

import ballerina/io;

function abc (string args) (int) {
    io:println("Hello, World! "+args);
    
    return 0;
}
