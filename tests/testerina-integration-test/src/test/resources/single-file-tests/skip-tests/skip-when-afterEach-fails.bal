import ballerina/test;
import ballerina/io;

// afterEach function that fails. All test functions except for the first should be skipped.

string a = "before";

@test:BeforeEach
public function beforeEach() {
    io:println("BeforeEach Func");
    a = a + "beforeEach";
}

@test:AfterEach
public function afterEach() {
    io:println("AfterEach Func");
    int i = 12/0;
}

@test:Config {}
public function test1() {
    io:println("test1");
    a = a + "test";
}

@test:Config {}
public function test2() {
    io:println("test2");
    a = a + "test";
}

@test:Config {}
public function test3() {
    io:println("test3");
    a = a + "test";
}

@test:AfterSuite {}
public function afterSuite() {
    io:println("Value of a is " + a); // expects a = "beforebeforeEachtest"
}