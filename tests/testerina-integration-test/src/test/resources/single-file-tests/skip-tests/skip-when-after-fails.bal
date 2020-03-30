
import ballerina/test;
import ballerina/io;

// Tests skipping of dependsOn functions when after func fails.

public function afterFunc() {
    // This will throw an exception and fail the after function
    int i = 12/0;
    io:println("After");
}

// This test should pass
@test:Config {
    after: "afterFunc"
}
public function test1() {
    io:println("test1");
}

// This should be skipped
@test:Config {
    dependsOn:["test1"]
}
public function test2() {
    io:println("test2");
}

// This test should pass
@test:Config {}
public function test3() {
    io:println("test3");
}

