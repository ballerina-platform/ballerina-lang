import ballerina/test;
import ballerina/io;

// Tests the behaviour when a non-existing function is provided as the dependsOn function

@test:Config {
    dependsOn:["non-existing"]
}
public function test2() {
    io:println("test2");
}
