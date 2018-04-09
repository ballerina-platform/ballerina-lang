import ballerina/test;
import ballerina/io;


// test
@test:Config {
    before:"beforeFunc-nonExist"
}
public function testFunc() {
    io:println("TestFunc");
}
