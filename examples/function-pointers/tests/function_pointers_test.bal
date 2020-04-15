import ballerina/test;

any[] outputs = [];

// This is the mock function which will replace the real function
@test:Mock {
    moduleName: "ballerina/io",
    functionName: "println"
}
public function mockPrint(any... s) {
    foreach var entry in s {
        outputs.push(entry);
    }
}

@test:Config {}
function testFunc() {
    // Invoking the main function
    main();
    test:assertEquals(outputs[0], "Answer: ");
    test:assertEquals(outputs[1], 280.0);
    test:assertEquals(outputs[2], "Answer: ");
    test:assertEquals(outputs[3], 280.0);
    test:assertEquals(outputs[4], "Answer: ");
    test:assertEquals(outputs[5], 280.0);
}
