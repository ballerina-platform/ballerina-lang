import ballerina/io;
import bar/Z;
  
# Prints `Hello World`.

public function main() {
    io:println("Hello world from module Y!");
    Z:main();
}

