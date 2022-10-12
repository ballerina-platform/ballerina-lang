import ballerina/io;

# Prints `Hello` with input string name.
#
# + name - the input sting name
# + return - "Hello, " with the input string name

public function hello() {
    io:println("Hello World");
}
