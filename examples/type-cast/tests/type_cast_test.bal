import ballerina/io;
import ballerina/test;

(any|error)[] outputs = [];
int counter = 0;

// This is the mock function that replaces the real function.
@test:Mock {
    moduleName: "ballerina/io",
    functionName: "println"
}
public function mockPrint(any|error... s) {
    foreach var elem in s {
        outputs[counter] = elem;
        counter += 1;
    }
}

@test:Config
function testFunc() {
    // Invoking the main function
    error? result = trap main();
    test:assertEquals(outputs[0], "Person Name: ");
    test:assertEquals(outputs[1], "Jane Doe");
    test:assertEquals(outputs[2], "Person Age: ");
    test:assertEquals(outputs[3], 25);
    test:assertEquals(outputs[4], "Employee Name: ");
    test:assertEquals(outputs[5], "Jane Doe");
    test:assertEquals(outputs[6], "Employee Age: ");
    test:assertEquals(outputs[7], 25);
    test:assertEquals(outputs[8], "Integer Value: ");
    test:assertEquals(outputs[9], 100);
    test:assertEquals(outputs[10], "Converted Float Value: ");
    test:assertEquals(outputs[11], 100.0);
    test:assertEquals(outputs[12], "Converted Float Value: ");
    test:assertEquals(outputs[13], 100.0);
    if (result is error) {
        test:assertEquals(result.reason(), "{ballerina}TypeCastError");
        string errorMessage = <string> result.detail().message;
        test:assertTrue(errorMessage.hasPrefix("incompatible types:"));
    } else {
        test:assertFail(msg = "expected cast to fail");
    }
}
