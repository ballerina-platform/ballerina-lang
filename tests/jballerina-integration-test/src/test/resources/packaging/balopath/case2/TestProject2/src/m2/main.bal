import ballerina/io;
import m1;

public function main() {
    m1:say();
    io:println("This line was added to suspend the import not used error");
}
