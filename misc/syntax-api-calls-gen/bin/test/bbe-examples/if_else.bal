import ballerina/io;

public function main() {
    int a = 10;
    int b = 0;

    // This is a basic `if` statement.
    if (a == 10) {
        io:println("a == 10");
    }

    // This is an `if-else` scenario.
    if (a < b) {
        io:println("a < b");
    } else {
        io:println("a >= b");
    }

    // This is an `if-else-if` ladder scenario.
    if (b < 0) {
        io:println("b < 0");
    } else if (b > 0) {
        io:println("b > 0");
    } else {
        io:println("b == 0");
    }
}
