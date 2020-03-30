import ballerina/io;

# Prints `Hello World`.
# Contains an if condition to test the code coverage

public function main() {
    // counter
    // test
    int a = 10;
    if (a == 10) {
        io:println("a is equal to 10");
    } else {
        io:println("a is notttttt equal to 10");
    }
   
    printHello();
}


// Prints hello
public function printHello() {
    // Prints hello
    io:println("Hello World!!");
}
