import ballerina/test;
import ballerina/io;

any[] outputs = [];
int count = 0;

// This is the mock function which will replace the real function
@test:Mock {
    moduleName: "ballerina/io",
    functionName: "println"
}
public function mockPrint(any... s) {
    outputs[count] = s[0];
    count += 1;
}

@test:Config
function testFunc() {
    // Invoking the main function
    main();
    test:assertEquals(outputs[0], 12);
    test:assertEquals(outputs[1], 4);
    test:assertEquals(outputs[2], 24);
    test:assertEquals(outputs[3], 13);
    test:assertEquals(outputs[4], 205);
    test:assertEquals(outputs[5], 19);
    test:assertEquals(outputs[6], 108);
    test:assertEquals(outputs[7], 3);
    test:assertEquals(outputs[8], 6);
}
