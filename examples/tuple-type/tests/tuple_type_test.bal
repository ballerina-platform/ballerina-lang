import ballerina/test;

(any|error)[] outputs = [];

// This is the mock function which will replace the real function
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
    // Invoking the main function
    main();
    [int, string] a = [10, "John"];
    test:assertEquals(outputs[0], a);
    test:assertEquals(outputs[1], 10);
    test:assertEquals(outputs[2], "John");
    test:assertEquals(outputs[3], "06/10: ");
    test:assertEquals(outputs[4], "quotient=");
    test:assertEquals(outputs[5], 0);
    test:assertEquals(outputs[6], " remainder=");
    test:assertEquals(outputs[7], 6);
    test:assertEquals(outputs[8], "57/10: ");
    test:assertEquals(outputs[9], "quotient=");
    test:assertEquals(outputs[10], 5);
    test:assertEquals(outputs[11], "09/10: ");
    test:assertEquals(outputs[12], "remainder=");
    test:assertEquals(outputs[13], 9);
}
