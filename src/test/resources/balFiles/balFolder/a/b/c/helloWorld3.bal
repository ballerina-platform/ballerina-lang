package a.b.c;

import ballerina.lang.system;

function abc (string args) (int) {
    system:println("Hello, World! "+args);
    
    return 0;
}