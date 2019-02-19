import ballerina/test;
import ballerina/io;

(any|error)?[] outputs = [];
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
    main();
    test:assertEquals(outputs[0], "Employee to Person, name: ");
    test:assertEquals(outputs[1], "Jack Sparrow");
    test:assertEquals(outputs[2], "map<any> to Person, name: ");
    test:assertEquals(outputs[3], "Jack Sparrow");
    test:assertEquals(outputs[4], "Error occurred on conversion");
    test:assertEquals(outputs[5], "int value: ");
    test:assertEquals(outputs[6], 45);
    test:assertEquals(outputs[7], "error: ");
    test:assertEquals(outputs[8], "'string' cannot be converted to 'int'");
    test:assertEquals(outputs[9], "int value: ");
    test:assertEquals(outputs[10], 10);
    test:assertEquals(outputs[11], "boolean value: ");
    test:assertEquals(outputs[12], true);
    test:assertEquals(outputs[13], "float value: ");
    test:assertEquals(outputs[14], 3.14);
}
