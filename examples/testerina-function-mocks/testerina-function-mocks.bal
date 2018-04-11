import ballerina/test;
import ballerina/io;

// THis is the mock function which will replace the real function
@test:Mock {
    // Since we don't have a package declaration, . is the current package
    packageName : "." ,
    functionName : "intAdd"
}
public function mockIntAdd (int a, int b) returns (int) {
    io:println("I'm the mock function!");
    return (a-b);
}

@test:Config{}
function testAssertIntEquals () {
    int answer = 0;
    answer = intAdd(5, 3);
    io:println("Function mocking test");
    test:assertEquals(answer, 2, msg = "function mocking failed");
}

public function intAdd (int a, int b) returns (int) {
    return (a + b);
}
