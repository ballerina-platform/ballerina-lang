import ballerina/test;

any[] outputs = [];

@test:Mock {
    moduleName: "ballerina/io",
    functionName: "println"
}
public function mockPrint(any... s) {
    outputs.push(s[0]);
}

@test:Config {}
function testFunc() {
    main();
    test:assertEquals(outputs[0], "Colombo");
    test:assertEquals(outputs[1], "UK");
}
