package a.b;

import ballerina.lang.system;

function main (string[] args) (int) {
    system:println("Hello, World! " + args[0]);
    
    return 0;
}
