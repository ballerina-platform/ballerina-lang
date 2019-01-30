
import ballerina/test;
import ballerina/io;

boolean success = false;

@test:Mock {
    moduleName: "ballerina/io",
    functionName: "println"
}
public function mockPrint(any... s) {
    io:println("io:println: Mocked");
    success = true;
}

@test:Config{}
function testAssertIntEquals () {
    printSomething();
    test:assertTrue(success, msg = "using same function inside mocking function failed");
}
