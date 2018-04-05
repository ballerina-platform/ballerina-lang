import ballerina/test;
import ballerina/io;

// This test should pass
@test:Config
public function test1() {
    io:println("test1");
}

// This test should fail and the consecutive depends on tests will be skipped
@test:Config {
    dependsOn:["test1"]
}
public function test2() {
    io:println("test2");
    int i = 12/0;
}

@test:Config {
    dependsOn:["test2"]
}
public function test3() {
    io:println("test3");
}

@test:Config {
    dependsOn:["test3"]
}
public function test4() {
    io:println("test4");
}

// This test should pass
@test:Config {}
public function test5() {
    io:println("test5");
}

// Failing the test to test the summary
@test:Config
public function test6() {
    io:println("test6");
    test:assertFail(msg = "Failing the Test");
}
