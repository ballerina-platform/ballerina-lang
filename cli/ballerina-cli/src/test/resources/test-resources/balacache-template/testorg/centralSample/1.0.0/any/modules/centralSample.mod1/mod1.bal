import ballerina/io;
# Returns the string `Hello` with the input string name.
#
# + name - name as a string
public function hello(string name) {
    if !(name is "") {
        io:println("Hello, " + name);
    }
    io:println("Hello, World!");
}
