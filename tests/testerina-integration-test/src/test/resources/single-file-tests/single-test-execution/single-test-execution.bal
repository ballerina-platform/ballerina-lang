import ballerina/test;
import ballerina/io;

// This tests the single test execution option

string testString = "";

@test:BeforeEach
function beforeEachFunc() {
    testString += "beforeEach";
}

// 2nd function
@test:Config {}
public function testFunc() {
    testString += "test";
}

// After function
@test:AfterEach
public function afterEachFunc() {
    testString += "afterEach";
}

// 2nd function
@test:Config {
    dependsOn:["testFunc"]
}
public function testFunc2() {
    io:println("TestFunc2");
    test:assertEquals(testString, "beforeEachtestafterEachbeforeEach");
}

// Disabled function
@test:Config {
    enable: true
}
public function testDisabledFunc() {
    io:println("testDisabledFunc");
    testString += "disabled";
    test:assertEquals(testString, "beforeEachdisabled");
}

// Test Dependent on disabled function
@test:Config {
    dependsOn: ["testDisabledFunc"]
}
public function testDependentDisabledFunc() {
    io:println("testDependentDisabledFunc");
    test:assertEquals(testString, "beforeEachdisabled");
}
