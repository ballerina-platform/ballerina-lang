import ballerina/test;
import ballerina/io;

string a = "before";

@test:AfterEach
public function afterEach() {
    io:println("AfterEach Func");
    a = a + "afterEach";
}

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
    a = a + "test1";
}

// Independent function which passes
@test:Config {
    dependsOn: ["test1"]
}
public function test2() {
    io:println("test2");
    a = a + "test2";
}

// Independent function - which fails
@test:Config {}
public function test3() {
    io:println("test3 - independent");
    a = a + "test3";
}

@test:AfterSuite {}
public function afterSuite() {
    io:println("Value of a is " + a); // expects a = "beforetest3afterEach"
}
