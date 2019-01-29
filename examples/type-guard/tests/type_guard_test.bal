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
    outputs[counter] = s[0];
    counter += 1;
}

@test:Config
function testFunc() {
    // Invoking the main function
    main();
    test:assertEquals(outputs[0], "value: value1");
    test:assertEquals(outputs[1], "value is ()");
    test:assertEquals(outputs[2], "key 'key3' not found");
    test:assertEquals(outputs[3], "Address: {company:\"Ballerina\", position:\"CEO\"}");
}
