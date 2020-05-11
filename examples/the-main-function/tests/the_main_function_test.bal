import ballerina/io;
import ballerina/test;

(any|error)[] outputs = [];
int counter = 0;

// This is the mock function which will replace the real function.
@test:Mock {
    moduleName: "ballerina/io",
    functionName: "println"
}
public function mockPrint(any|error... s) {
    foreach var val in s {
        outputs[counter] = val;
        counter += 1;
    }
}

@test:Config
function testFunc() {
    // Invoking the main function.
    error? e = main("add", 10, 20);
    test:assertTrue(e is ());
    test:assertEquals(outputs[0], "Result: ");
    test:assertEquals(outputs[1], 30);

    // Invoking the main function.
    counter = 0;
    e = main("subtract", initialValue = 40, 10, 20);
    test:assertTrue(e is ());
    test:assertEquals(outputs[0], "Result: ");
    test:assertEquals(outputs[1], 10);

    // Invoking the main function.
    counter = 0;
    e = main("unknown op");
    if (e is error) {
        test:assertEquals(e.reason(), "unknown operation");
        test:assertEquals(outputs[0], "Error: Unknown Operation");
    } else {
        test:assertFail(msg = "expected an error");
    }
}
