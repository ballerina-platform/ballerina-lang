package a.b;

import ballerina.io;

function xyz (string args) (int) {
    io:println("Hello, World! "+args);
    
    return 0;
}
