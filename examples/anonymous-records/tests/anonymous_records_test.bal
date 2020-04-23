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
    test:assertEquals(outputs[0], "name=John Doe age=25 address=city=Colombo country=Sri Lanka");
    test:assertEquals(outputs[1], "name=Jane Doe age=20 address=city=London country=UK");
    test:assertEquals(outputs[2], "John Doe 25 city=Colombo country=Sri Lanka");
}
