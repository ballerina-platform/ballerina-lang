import ballerina/io;

public function main(string... args) {
    sourceFunc2();
    io:println("Hello, World2! 1");
}

function sourceFunc2() {
    io:println("Hello, World2! 2");
}