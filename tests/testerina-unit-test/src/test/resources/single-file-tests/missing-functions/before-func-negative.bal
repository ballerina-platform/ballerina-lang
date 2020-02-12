import ballerina/test;
import ballerina/io;

// test
@test:Config {
    before:"beforeFunc-nonExist"
}
public function beforeFuncNegative() {
    io:println("TestFunc");
}
