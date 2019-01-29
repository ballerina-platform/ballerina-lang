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
    outputs[counter] = string.convert(s[0]) + string.convert(s[1]);
    counter += 1;
}

@test:Config
function testFunc() {
    // Invoking the main function.
    main();
    test:assertEquals(outputs[0], "value: 1");
    test:assertEquals(outputs[1], "value += 7: 8");
    test:assertEquals(outputs[2], "value -= 2: 6");
    test:assertEquals(outputs[3], "value /= 3: 2");
    test:assertEquals(outputs[4], "value *= 2: 4");
    test:assertEquals(outputs[5], "value &= 4: 4");
    test:assertEquals(outputs[6], "value |= 3: 7");
    test:assertEquals(outputs[7], "value ^= 5: 2");
    test:assertEquals(outputs[8], "value <<= 1: 4");
    test:assertEquals(outputs[9], "value >>= 1: 2");
    test:assertEquals(outputs[10], "value >>>= 1: 1");
}
