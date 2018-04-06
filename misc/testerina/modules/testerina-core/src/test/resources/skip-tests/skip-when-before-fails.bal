import ballerina/test;
import ballerina/io;

// before function
public function before() {
    io:println("Before Func");
    int i = 12/0;
}

@test:Config {
    before:"before"
}
public function test1() {
    io:println("test1");
}

// Independent function which passes
@test:Config
public function test3() {
    io:println("test3 - independent");
}

// Independent function - which fails
@test:Config
public function test4() {
    io:println("test4 - independent");
    test:assertFail();
}
