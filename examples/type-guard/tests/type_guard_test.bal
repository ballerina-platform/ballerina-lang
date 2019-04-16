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
    foreach var value in s {
        outputs[counter] = value;
        counter += 1;
    }
}

@test:Config
function testFunc() {
    // Invoking the main function
    main();
    test:assertEquals(outputs[0], "value is an int: ");
    test:assertEquals(outputs[1], 10);
    test:assertEquals(outputs[2], "value + 1: ");
    test:assertEquals(outputs[3], 11);
    test:assertEquals(outputs[4], "- value is string|boolean: ");
    test:assertEquals(outputs[5], "Hello World");
}
