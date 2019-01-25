import ballerina/test;
import ballerina/io;

any[] outputs = [];
int counter = 0;

// This is the mock function that replaces the real function.
@test:Mock {
    moduleName: "ballerina/io",
    functionName: "println"
}
public function mockPrint(any... s) {
    foreach var entry in s {
        outputs[counter] = entry;
        counter += 1;
    }
}

@test:Config
function testFunc() {
    // Invoke the main function.
    main();
    test:assertEquals(outputs[0], "Error: ");
    test:assertEquals(outputs[1], "Account Not Found");
    test:assertEquals(outputs[2], ", Account ID: ");
    test:assertEquals(outputs[3], -1);
}
