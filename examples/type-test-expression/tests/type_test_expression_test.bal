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
    // Invoking the main function.
    main();
    io:println(outputs[1]);
    test:assertEquals(outputs[0], "Is 'a' a string? ");
    test:assertEquals(outputs[1], true);
    test:assertEquals(outputs[2], "'a' is a string with value: ");
    test:assertEquals(outputs[3], "Hello, world!");
    test:assertEquals(outputs[4], "Alex is a student");
    test:assertEquals(outputs[5], "Alex is a person");
    test:assertEquals(outputs[6], "Alex is not a vehicle");
    test:assertEquals(outputs[7], "Is foo returns a student?");
    test:assertEquals(outputs[8], true);
    test:assertEquals(outputs[9], "Is foo returns a student?");
    test:assertEquals(outputs[10], false);
}
