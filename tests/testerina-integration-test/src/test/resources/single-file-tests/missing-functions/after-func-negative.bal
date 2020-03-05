import ballerina/test;
import ballerina/io;

// Tests the behaviour when a non-exesting function is provided as the after function

@test:Config {
    after:"afterFunc-nonExist"
}
public function afterFuncNegative() {
    io:println("TestFunc");
}
