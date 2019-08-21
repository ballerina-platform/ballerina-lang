import ballerina/io;
import baz/bee;

public function say() {
    bee:say();
    io:println("This is org2/m1");
}
