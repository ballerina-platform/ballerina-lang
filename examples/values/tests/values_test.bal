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
    test:assertEquals(outputs[0], 10);
    test:assertEquals(outputs[1], 100);
    test:assertEquals(outputs[2], 20.0);
    test:assertEquals(outputs[3], "Max float: ");
    test:assertEquals(outputs[4], 22.0);
    test:assertEquals(outputs[5], true);
    test:assertEquals(outputs[6], true);
    test:assertEquals(outputs[7], true);
    test:assertEquals(outputs[8], 27.5d);
    test:assertEquals(outputs[9], 23);
    test:assertEquals(outputs[10], "Ballerina");
    test:assertEquals(outputs[11], true);
}
