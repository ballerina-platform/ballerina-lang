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
    test:assertEquals(0, outputs[0]);
    test:assertEquals(1, outputs[1]);
    test:assertEquals(2, outputs[2]);
    test:assertEquals(0, outputs[3]);
    test:assertEquals(1, outputs[4]);
    test:assertEquals(2, outputs[5]);
    test:assertEquals(3, outputs[6]);
    test:assertEquals(4, outputs[7]);
}
