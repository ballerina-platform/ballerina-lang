import ballerina/test;
import ballerina/io;

any|error[] outputs = [];
int counter = 0;

// This is the mock function that will replace the real function.
@test:Mock {
    moduleName: "ballerina/io",
    functionName: "println"
}
public function mockPrint(any|error... s) {
    outputs[counter] = s;
    counter += 1;
}

@test:Config
function testFunc() {
    // Invoking the main function.
    main();
    test:assertEquals(outputs[0], "Mary");
    test:assertEquals(outputs[1], "Colombo 03");

    error e = <error> outputs[2];
    test:assertEquals(e.reason(), "{ballerina}KeyNotFound");
    test:assertEquals(e.detail()?.message, "Key 'age' not found in JSON mapping");
    test:assertEquals(outputs[3], ());
    test:assertEquals(outputs[4], ());
}

