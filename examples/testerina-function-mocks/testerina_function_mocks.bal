import ballerina/test;
import ballerina/io;

// This is the mock function which will replace the real function.
@test:Mock {
    // Since we don't have a package declaration, `.` is the current package
    // We can include any package here e.g : `ballerina/io`
    packageName: ".",
    functionName: "intAdd"
}
// The mock function's signature should match with the actual function's signature.
public function mockIntAdd(int a, int b) returns (int) {
    io:println("I'm the mock function!");
    return (a - b);
}

// This is the test function.
@test:Config {}
function testAssertIntEquals() {
    int answer = 0;
    answer = intAdd(5, 3);
    io:println("Function mocking test");
    test:assertEquals(answer, 2, msg = "function mocking failed");
}

// The real function which is mocked above.
public function intAdd(int a, int b) returns (int) {
    return (a + b);
}
