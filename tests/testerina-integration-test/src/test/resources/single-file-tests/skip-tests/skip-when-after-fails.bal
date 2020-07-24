
import ballerina/test;
import ballerina/io;

// Tests skipping of dependsOn functions when after func fails.

string a = "before";

@test:AfterEach
public function afterEach() {
    io:println("After Each");
    a = a + "afterEach";
}

public function afterFunc() {
    // This will throw an exception and fail the after function
    io:println("After");
    int i = 12/0;
}

// This test should pass
@test:Config {
    after: "afterFunc"
}
public function test1() {
    io:println("test1");
    a = a + "test";
}

// This should be skipped
@test:Config {
    dependsOn:["test1"]
}
public function test2() {
    io:println("test2");
    a = a + "test2";
}

// This test should pass
@test:Config {}
public function test3() {
    io:println("test3 - independant");
    a = a + "test";
}

@test:AfterSuite {}
public function afterSuite() {
    io:println("Value of a is " + a); // expects a = "beforetestafterEachtestafterEach"
}

