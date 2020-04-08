import ballerina/test;
import ballerina/io;

// This tests the functionality of dependsOn attribute in a test
// using string concatenation

string testString = "";

// 2nd function
@test:Config {
    dependsOn:["test1"]
}
public function test2() {
    testString = testString + "test2";
    io:println("test2");
}

// 1st function
@test:Config {}
public function test1() {
    testString = testString + "test1";
    io:println("test1");
}

// 3rd function
@test:Config {
    dependsOn:["test2"]
}
public function test3() {
    testString = testString + "test3";
    io:println("test3");
}

// Last function
@test:Config {
    dependsOn:["test3"]
}
public function test4() {
    test:assertEquals(testString, "test1test2test3", msg = "Order is not correct");
    io:println("test4");
}
