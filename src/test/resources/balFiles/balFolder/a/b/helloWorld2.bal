package a.b;

import ballerina.lang.system;

function xyz (string args) (int) {
    system:println("Hello, World! "+args);
    
    return 0;
}