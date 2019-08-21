import ballerina/io;
import bar/bee;

public function main() {
    bee:say();
    io:println("This line was added to suspend the import not used error");

}
