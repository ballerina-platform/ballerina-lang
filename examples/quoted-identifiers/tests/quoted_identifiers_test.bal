import ballerina/test;

any[] outputs = [];
int counter = 0;

// This is the mock function that will replace the real function.
@test:Mock {
    moduleName: "ballerina/io",
    functionName: "println"
}
public function mockPrint(any... s) {
    outputs[counter] = s[0];
    counter += 1;
}

@test:Config {}
function testFunc() {
    main();
    test:assertEquals(outputs[0], 2);
    test:assertEquals(outputs[1], 3);
}
