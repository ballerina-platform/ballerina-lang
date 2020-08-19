import ballerina/io;

# Description
#
# + name - name Parameter Description
public function sayHello(string name) {
    io:println("Hello " + name + "!");
}
