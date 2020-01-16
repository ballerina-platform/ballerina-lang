import ballerina/io;
import wso2/utils;

function testAcceptNothingButReturnString() returns handle {
    return utils:getString();
}

public function main() {
    io:println(testAcceptNothingButReturnString());
}
