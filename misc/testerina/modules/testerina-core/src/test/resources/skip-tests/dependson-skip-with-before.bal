package pck1;

import ballerina/test;
import ballerina/io;

// Tests skipping of dependsOn functions when before func fails.
public function beforeFunc() {
    // This will throw an exception and fail the before function
    int i = 12/0;
    io:println("Before");
}

@test:Config {
    before : "beforeFunc"
}
public function test1() {
    io:println("test1");
}

@test:Config {
    dependsOn:["test1"]
}
public function test2() {
    io:println("test2");
}
//
//@test:Config {
//    dependsOn:["test2"]
//}
//public function test3() {
//    io:println("test3");
//}

// This test should pass
@test:Config {}
public function test4() {
    io:println("test3");
}

//// Failing the test to test the summary
//@test:Config
//public function test5() {
//    test:assertFail(msg = "Failing the Test");
//}
