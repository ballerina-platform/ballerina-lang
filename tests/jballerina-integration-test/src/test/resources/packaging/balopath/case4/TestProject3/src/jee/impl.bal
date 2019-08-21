import ballerina/io;
import bcintegrationtest/fee;

public function main() {
    fee:say();
    io:println("This line was added to suspend the import not used error");
}
