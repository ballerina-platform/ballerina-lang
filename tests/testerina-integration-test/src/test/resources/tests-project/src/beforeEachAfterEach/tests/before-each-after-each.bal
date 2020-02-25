import ballerina/test;
import ballerina/io;

// This tests the functionality of beforeEach and afterEach functions in a test
// using string concatenation

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

// 2nd function
@test:Config {
    dependsOn:["testFunc2"]
}
public function testFunc3() {
    io:println("TestFunc3");
    test:assertEquals(testString, "beforeEachtestafterEachbeforeEachafterEachbeforeEach");
}
