import ballerina/java;
import wso2/utils;

function testAcceptNothingButReturnString() returns handle {
    return utils:getString();
}

public function main() {
    string s = <string>java:toString(testAcceptNothingButReturnString());
}
