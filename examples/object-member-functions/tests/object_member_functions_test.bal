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
    outputs[counter] = string.convert(s[0]);
    counter += 1;
}

@test:Config
function testFunc() {
    // Invoke the main function.
    main();
    test:assertEquals(outputs[0], "{age:5, firstName:\"John\", lastName:\"Doe\"}");
    test:assertEquals(outputs[1], "John Doe");
    test:assertEquals(outputs[2], "{age:50, firstName:\"John\", lastName:\"Doe\"}");
}
