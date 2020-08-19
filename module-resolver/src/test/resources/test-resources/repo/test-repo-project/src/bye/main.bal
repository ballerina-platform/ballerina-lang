import ballerina/io;

# Description
#
# + name - name Parameter Description
public function sayBye(string name) {
    io:println("Bye " + name + "!");
}
