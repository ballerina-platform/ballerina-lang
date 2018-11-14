import ballerina/io;

public function main(string... args) {

    boolean[] booleanValues = [true, false];

    foreach boolVal in booleanValues {
        sourceFunc2(boolVal);
    }

    io:println("Hello, World2! 1");
}

function sourceFunc2(boolean boolVal) {
    io:println("Hello, World2! 2");

    if (boolVal) {
        io:println("Hello, World2! 2 if block boolVal: " + boolVal);
    } else {
        io:println("Hello, World2! 2 else block boolVal: " + boolVal);
    }
}
