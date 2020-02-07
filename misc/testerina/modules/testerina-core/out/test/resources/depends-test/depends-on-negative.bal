import ballerina/test;
import ballerina/io;


// 2nd function
@test:Config {
    dependsOn:["non-existing"]
}
public function test2() {
    io:println("test2");
}
