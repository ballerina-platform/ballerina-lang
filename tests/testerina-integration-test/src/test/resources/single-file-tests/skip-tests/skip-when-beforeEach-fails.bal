import ballerina/test;
import ballerina/io;

// before function that fails. All test functions should be skipped due to this.
@test:BeforeEach
public function beforeEach() {
    io:println("BeforeEach Func");
    int i = 12/0;
}

@test:Config {}
public function test1() {
    io:println("test1");
}

@test:Config {}
public function test2() {
    io:println("test2");
}

@test:Config {}
public function test3() {
    io:println("test3");
}
