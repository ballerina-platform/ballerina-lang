import ballerina/io;

# Prints `Hello World`.

public const string fooStr1 = "this is a foo string";

public function main() {
    io:println("Hello World!");
}

public function fooFn() returns string {
    io:println("invoked fooFn");
    return "fooFn invoked";
}
