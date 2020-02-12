import ballerina/test;
import ballerina/io;

// test
@test:Config {
    after:"afterFunc-nonExist"
}
public function afterFuncNegative() {
    io:println("TestFunc");
}
