import ballerina/io;

function main(string... args) {
    int a = 10;
    int b = 0;

    // The below scenario shows a basic if statement
    if (a == 10) {
        io:println("a == 10");
    }

    // If-else scenario
    if (a < b) {
        io:println("a < b");
    } else {
        io:println("a >= b");
    }

    // Else-if scenario.
    if (b < 0) {
        io:println("b < 0");
    } else if (b > 0) {
        io:println("b > 0");
    } else {
        io:println("b == 0");
    }
}
