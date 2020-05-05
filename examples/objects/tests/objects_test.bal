import ballerina/test;

(any|error)[] outputs = [];

// This is the mock function that replaces the real function.
@test:Mock {
    moduleName: "ballerina/io",
    functionName: "println"
}
public function mockPrint(any|error... s) {
    outputs.push(s[0]);
}

@test:Config {}
function testFunc() {
    // Invoke the main function.
    main();
    test:assertEquals(outputs[0], 0);
    test:assertEquals(outputs[1], 0);
    test:assertEquals(outputs[2], 0);
}
