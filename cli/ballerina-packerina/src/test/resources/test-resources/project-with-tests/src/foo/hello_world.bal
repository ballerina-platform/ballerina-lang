import ballerina/io;

public function main() {
    io:println(getHelloWorld());
}

public function getHelloWorld() returns string {
    return "Hello, World!";
}