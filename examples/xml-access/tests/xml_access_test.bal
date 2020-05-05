import ballerina/test;

(any|error)[] outputs = [];
int counter = 0;

// This is the mock function that replaces the real function
@test:Mock {
    moduleName: "ballerina/io",
    functionName: "println"
}
public function mockPrint(any|error... s) {
    outputs[counter] = s[0];
    counter += 1;
}

@test:Config
function testFunc() {
    // Invoking the main function.
    main();

    test:assertEquals(string.convert(outputs[0]), "<fname title=\"Sir\">Arthur</fname>");
    test:assertEquals(string.convert(outputs[1]), "<fname title=\"Sir\">Arthur</fname>");
    test:assertEquals(string.convert(outputs[2]), "");
    test:assertEquals(string.convert(outputs[3]), "");
    test:assertEquals(string.convert(outputs[4]), "Arthur");
    test:assertEquals(string.convert(outputs[5]), "Arthur");
    test:assertEquals(string.convert(outputs[6]), "Sir");
    test:assertEquals(string.convert(outputs[7]), "Sir");
}
