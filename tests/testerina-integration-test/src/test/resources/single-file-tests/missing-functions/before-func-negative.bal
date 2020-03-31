import ballerina/test;
import ballerina/io;

// Tests the behaviour when a non-exesting function is provided as the before function

@test:Config {
    before:"beforeFunc-nonExist"
}
public function beforeFuncNegative() {
    io:println("TestFunc");
}
