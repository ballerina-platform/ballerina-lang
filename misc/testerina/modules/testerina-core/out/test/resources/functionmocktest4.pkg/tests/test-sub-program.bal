
import ballerina/test;
import ballerina/io;

boolean success = false;

@test:Mock {
    moduleName: "ballerina/io",
    functionName: "println"
}
public function mockPrint(any... s) {
    io:println("The function 'io:println' was mocked");
    success = true;
}

@test:Config{}
function testAssertIntEquals () {
    printSomething();
    test:assertTrue(success, msg = "using the same function inside the mocking function failed");
}
