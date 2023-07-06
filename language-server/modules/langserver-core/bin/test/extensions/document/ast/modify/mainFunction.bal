import ballerina/io;

public function main() {
    io:println("Hello, World!");
    int gg = myFunction();
}

public function myFunction() returns int {
    return 123;
}
