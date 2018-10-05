import ballerina/test;
import ballerina/io;

any[] outputs = [];
int count = 0;

// This is the mock function which will replace the real function
@test:Mock {
    packageName: "ballerina/io",
    functionName: "println"
}
public function mockPrint(any... s) {
    outputs[count] = s[0];
    count++;
}

@test:Config
function testFunc() {
    // Invoking the main function
    main();
    test:assertEquals(outputs[0], 0);
    test:assertEquals(outputs[1], 1);
    test:assertEquals(outputs[2], 8);
    test:assertEquals(outputs[3], 23);
    test:assertEquals(outputs[4], 1000);
    test:assertEquals(outputs[5], 3);
    test:assertEquals(outputs[6], 3);
    test:assertEquals(outputs[7], 9);
    test:assertEquals(outputs[8], 5);
    test:assertEquals(outputs[9], 5);
    test:assertEquals(outputs[10], 4);
}
