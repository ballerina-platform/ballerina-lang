import ballerina/test;
import ballerina/io;

string testString = "";

function beforeFunc() {
    io:println("Before function");
    testString += "before";
}

// 2nd function
@test:Config {
    before:"beforeFunc",
    after:"afterFunc"
}
public function testFunc() {
    testString += "test";
    io:println("TestFunc");
}

// After function
public function afterFunc() {
    testString += "after";
    io:println("test1");
}

// 2nd function
@test:Config {
    dependsOn:["testFunc"]
}
public function testFunc2() {
    io:println("TestFunc");
    test:assertEquals(testString, "beforetestafter");
}