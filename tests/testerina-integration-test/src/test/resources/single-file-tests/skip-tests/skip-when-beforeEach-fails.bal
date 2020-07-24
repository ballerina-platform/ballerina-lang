import ballerina/test;
import ballerina/io;

// before function that fails. All test functions should be skipped due to this.

string a = "before";

@test:BeforeEach
public function beforeEach() {
    io:println("BeforeEach Func");
    int i = 12/0;
}

@test:AfterEach
public function AfterEach() {
    io:println("AfterEach Func");
    a = a + "afterEach";
}

@test:Config {}
public function test1() {
    io:println("test1");
    a = a + "test1";
}

@test:Config {}
public function test2() {
    io:println("test2");
    a = a + "test2";
}

@test:Config {}
public function test3() {
    io:println("test3");
    a = a + "test3";
}

@test:AfterSuite {}
public function afterSuite() {
    io:println("Value of a is " + a);
}
