import ballerina/test;
import ballerina/io;

any[] outputs = [];
int counter = 0;

// This is the mock function which will replace the real function.
@test:Mock {
    moduleName: "ballerina/io",
    functionName: "println"
}
public function mockPrint(any... s) {
    outputs[counter] = <string>s[0] + string.convert(s[1]);
    counter += 1;
}

@test:Config
function testFunc() {
    // Invoking the main function.
    main();
    test:assertEquals(outputs[0], "initial value: 1");
    test:assertEquals(outputs[1], "add: 8");
    test:assertEquals(outputs[2], "subtract: 6");
    test:assertEquals(outputs[3], "divide: 2");
    test:assertEquals(outputs[4], "multiply: 4");
    test:assertEquals(outputs[5], "and: 4");
    test:assertEquals(outputs[6], "or: 7");
    test:assertEquals(outputs[7], "xor: 2");
    test:assertEquals(outputs[8], "left shift: 4");
    test:assertEquals(outputs[9], "right shift: 2");
    test:assertEquals(outputs[10], "logical shift: 1");
}
