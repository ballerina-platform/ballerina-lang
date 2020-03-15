import ballerina/test;

any[] outputs = [];
int count = 0;

@test:Mock {
    moduleName: "ballerina/io",
    functionName: "println"
}
public function mockPrint(any... s) {
    outputs[count] = s[0].toString();
    count += 1;
}

@test:Config {}
function testFunc() {
    main();
    test:assertEquals(outputs[0], "Colombo");
    test:assertEquals(outputs[1], "UK");
}
