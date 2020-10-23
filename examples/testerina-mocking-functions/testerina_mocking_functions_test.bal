// This demonstrates different ways to mock functions.
import ballerina/test;

(any|error)[] outputs = [];

@test:Mock {
    // This specifies a mock function that should replace the
    // imported `io:println` function.
    moduleName: "ballerina/io",
    functionName: "println"
}
function mockIoPrintLn((any|error)... text) {
    outputs.push(text);
}

@test:Config {}
function testMathConsts() {
   // This function call to the `io:println` will be replaced with the `mockIoPrintLn` function.
   printMathConsts();
   test:assertEquals(outputs[0].toString(), "Value of PI :  3.141592653589793");
}

// This creates an object for stubbing calls to the intAdd` function
// which is written in the same module.
@test:MockFn { functionName: "intAdd" }
test:MockFunction intAddMockFn = new();

@test:Config {}
function testAssertIntEquals() {
    // This stubs the calls to the `intAdd` function to return the specified value.
    test:when(intAddMockFn).thenReturn(20);

    // This stubs the calls to the `intAdd` function to return the specified value
    // when the specified arguments are provided.
    test:when(intAddMockFn).withArguments(0, 0).thenReturn(-1);

    test:assertEquals(intAdd(10, 6), 20, msg = "function mocking failed");
    test:assertEquals(intAdd(0, 0), -1,
        msg = "function mocking with arguments failed");

    // This stubs the calls to the `intAdd` function to invoke the specified function.
    test:when(intAddMockFn).call("mockIntAdd");

    test:assertEquals(addValues(11, 6), 5, msg = "function mocking failed");
}

// This is a mock function that can be called
// in place of the `intAdd` function.
public function mockIntAdd(int a, int b) returns int {
    return (a - b);
}
