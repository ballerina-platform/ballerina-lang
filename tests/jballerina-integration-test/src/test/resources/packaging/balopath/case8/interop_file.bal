import bcintegrationtest/bee;
import ballerina/io;

function testAcceptNothingButReturnString() returns handle {
    return bee:getString();
}

public function main() {
    io:println(testAcceptNothingButReturnString());
}
