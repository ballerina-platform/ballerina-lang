import ballerina/test;
import ballerina/io;

// This tests the functionality of before and after functions in a test
// using string concatenation

string testString = "";

function beforeFunc() {
    testString += "before";
}

// 2nd function
@test:Config {
    before:"beforeFunc",
    after:"afterFunc"
}
public function testFunc() {
    testString += "test";
}

// After function
public function afterFunc() {
    testString += "after";
}

// 2nd function
@test:Config {
    dependsOn:["testFunc"]
}
public function testFunc2() {
    io:println("TestFunc2");
    test:assertEquals(testString, "beforetestafter");
}
