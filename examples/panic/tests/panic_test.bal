import ballerina/test;
import ballerina/io;

any[] outputs = [];
int counter = 0;

// This is the mock function which will replace the real function
@test:Mock {
    moduleName: "ballerina/io",
    functionName: "println"
}
public function mockPrint(any... s) {
    foreach var val in s {
        outputs[counter] = val;
        counter += 1;
    }
}

@test:Config
function testFunc() {
    // Invoking the main function
    error? e = trap main();
    test:assertEquals(outputs[0], "Record ID: ");
    test:assertEquals(outputs[1], 1);
    test:assertEquals(outputs[2], ", value: ");
    test:assertEquals(outputs[3], "record1");
    test:assertTrue(e is error, msg = "expected main to panic");
    if (e is error) {
        test:assertEquals(e.reason(), "Record is nil");
    }
}
