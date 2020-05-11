import ballerina/test;

(any|error)[] outputs = [];

// This is the mock function which will replace the real function.
@test:Mock {
    moduleName: "ballerina/io",
    functionName: "println"
}
public function mockPrint(any|error... s) {
    foreach var entry in s {
    	 outputs.push(entry);
    }
}

@test:Config {}
function testFunc() {
    // Invoking the main function.
    main();
    test:assertEquals(outputs[0], "value: ");
    test:assertEquals(outputs[1], 1);
    test:assertEquals(outputs[2], "value += 7: ");
    test:assertEquals(outputs[3], 8);
    test:assertEquals(outputs[4], "value -= 2: ");
    test:assertEquals(outputs[5], 6);
    test:assertEquals(outputs[6], "value /= 3: ");
    test:assertEquals(outputs[7], 2);
    test:assertEquals(outputs[8], "value *= 2: ");
    test:assertEquals(outputs[9], 4);
    test:assertEquals(outputs[10], "value &= 4: ");
    test:assertEquals(outputs[11], 4);
    test:assertEquals(outputs[12], "value |= 3: ");
    test:assertEquals(outputs[13], 7);
    test:assertEquals(outputs[14], "value ^= 5: ");
    test:assertEquals(outputs[15], 2);
    test:assertEquals(outputs[16], "value <<= 1: ");
    test:assertEquals(outputs[17], 4);
    test:assertEquals(outputs[18], "value >>= 1: ");
    test:assertEquals(outputs[19], 2);
    test:assertEquals(outputs[20], "value >>>= 1: ");
    test:assertEquals(outputs[21], 1);
}
