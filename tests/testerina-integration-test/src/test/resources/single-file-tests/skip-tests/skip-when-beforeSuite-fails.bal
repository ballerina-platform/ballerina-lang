import ballerina/test;
import ballerina/io;

// before function
@test:BeforeSuite
public function beforeSuite() {
    io:println("Before Suite Function");
    int i = 12/0;
}

public function before() {
    io:println("Before function");
}

@test:Config {}
public function test1() {
    io:println("test1");
}

// Independent function which passes
@test:Config {}
public function test3() {
    io:println("test3 - independent");
}

// Independent function - which fails
@test:Config {}
public function test4() {
    io:println("test4 - independent");
    test:assertFail();
}

