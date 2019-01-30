import ballerina/io;

// this function should not get called as mocked
public function printSomething() {
    io:println("something");
}
