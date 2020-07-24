// This file is used to test the behavior when there is a failing BeforeSuite function.
import ballerina/test;
import ballerina/io;

string a = "before";

@test:BeforeSuite
public function beforeSuite() {
    io:println("Failing Before Suite");
    int i = 12/0; // This will throw an exception and fail the function
}

@test:AfterSuite {}
public function afterSuite() {
    a = a + "after";
    io:println("After Suite");
}

@test:AfterSuite { alwaysRun : true}
public function afterSuiteAlwaysRun() {
    io:println("After Suite AlwaysRun");
    io:println("Value of a is " + a);
}

@test:BeforeEach
public function beforeEach() {
    io:println("Before each");
    a = a + "beforeEach";
}

@test:AfterEach
public function afterEach() {
    io:println("After each");
    a = a + "afterEach";
}

public function beforeFunc() {
    io:println("Before");
    a = a + "before";
}

public function afterFunc() {
    io:println("After");
    a = a + "after";
}

@test:Config {
    before: "beforeFunc",
    after: "afterFunc"
}
public function test1() {
    io:println("test1");
    a = a + "test1";
}

@test:Config {
    dependsOn:["test1"]
}
public function test2() {
    io:println("test2");
    a = a + "test2";
}

@test:Config {}
public function test3() {
    io:println("test3");
    a = a + "test3";
}
