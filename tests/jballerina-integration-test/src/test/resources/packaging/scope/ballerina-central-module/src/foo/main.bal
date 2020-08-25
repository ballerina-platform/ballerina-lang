import ballerina/io;
import ballerina/socket;

public function main() {
    io:println(socket:sayHello("World"));
}
