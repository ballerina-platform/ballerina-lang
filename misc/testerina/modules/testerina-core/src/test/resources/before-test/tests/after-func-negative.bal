import ballerina/test;
import ballerina/io;


// test
@test:Config {
    before:"afterFunc-nonExist"
}
public function testFunc() {
    io:println("TestFunc");
}
