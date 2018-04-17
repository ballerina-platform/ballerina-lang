import ballerina/lang.system;

function main (string... args) {
    int a = 10;
    int b = 0;

    //Here’s a basic example.
    if (a == 10) {
        system:println("a == 10");
    }

    //If else scenario.
    if (a < b) {
        system:println("a < b");
    } else {
        system:println("a >= b");
    }

    //Else-if scenario.
    if (b < 0) {
        system:println("b < 0");
    } else if (b > 0) {
        system:println("b > 0");
    } else {
        system:println("b == 0");
    }
}
