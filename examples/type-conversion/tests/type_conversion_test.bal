import ballerina/test;
import ballerina/io;

any[] outputs = [];
int counter = 0;

// This is the mock function which will replace the real function
@test:Mock {
    packageName: "ballerina/io",
    functionName: "println"
}
public function mockPrint(any... s) {
    outputs[counter] = s[0];
    counter++;
}

@test:Config
function testFunc() {
    // Invoking the main function
    main();
    test:assertEquals(10, outputs[0]);
    test:assertEquals("error: 'string' cannot be converted to 'int'", outputs[1]);
    test:assertEquals(1, outputs[2]);
    test:assertEquals(true, outputs[3]);
    test:assertEquals(true, outputs[4]);
    test:assertEquals(3.14, outputs[5]);
}
