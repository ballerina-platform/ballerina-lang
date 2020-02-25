import ballerina/test;
import ballerina/io;

// afterEach function that fails. All test functions except for the first should be skipped.
@test:AfterEach
public function afterEach() {
    io:println("AfterEach Func");
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
