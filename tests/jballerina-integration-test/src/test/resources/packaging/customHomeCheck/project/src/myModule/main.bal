import ballerina/io;
import customCheck/myImport;

# Prints `Hello World`.

public function main() {
    io:println("Calling import function");
    myImport:hello();
}
