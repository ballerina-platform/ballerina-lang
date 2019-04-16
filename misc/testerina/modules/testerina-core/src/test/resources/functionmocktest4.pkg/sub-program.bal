import ballerina/io;

// This function should not get called as it is being mocked.
public function printSomething() {
    io:println("something");
}
