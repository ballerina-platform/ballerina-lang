import ballerina/test;
import ballerina/io;

// This tests the behavior when there is a test with before and dependsOn
// using string concatenation

string testStr = "";

function before() {
    testStr = testStr + "before";
    io:println("Before Tests");
}

// 2nd function
@test:Config {
    dependsOn:["testWithBefore1"]
}
public function testWithBefore2() {
    testStr = testStr + "test2";
    io:println("testWithBefore2");
}

// 1st function
@test:Config {
    before: "before",
    dependsOn: ["test4"] // added to preserve order of test execution 
}
public function testWithBefore1() {
    testStr = testStr + "test1";
    io:println("testWithBefore1");
}

// 3rd function
@test:Config {
    dependsOn:["testWithBefore2"]
}
public function testWithBefore3() {
    testStr = testStr + "test3";
    io:println("testWithBefore3");
}

// Last function
@test:Config {
    dependsOn:["testWithBefore3"]
}
public function testWithBefore4() {
    test:assertEquals(testStr, "beforetest1test2test3", msg = "Order is not correct");
    io:println("testWithBefore4");
}
