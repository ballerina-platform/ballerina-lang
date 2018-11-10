import ballerina/io;
import bar;

# Prints `Hello World`.

public function main() {
    string greeting = bar:greeting();	
    io:println(greeting);
}
