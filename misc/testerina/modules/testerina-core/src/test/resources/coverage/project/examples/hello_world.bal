import ballerina/io;

public function main(string... args) {
    sourceFunc();
    io:println("Hello, World! 1");
}

function sourceFunc() {
    io:println("Hello, World! 2");
}
