import ballerina/test;

(any|error)[] outputs = [];
int counter = 0;

// This is the mock function which will replace the real function
@test:Mock {
    moduleName: "ballerina/io",
    functionName: "println"
}
public function mockPrint(any|error... s) {
    foreach var v in s {
        outputs[counter] = v;
        counter += 1;
    }
}

@test:Config {}
function testFunc() {
    // Invoking the main function
    main();
    test:assertEquals(outputs[0], "m1 === m2: ");
    test:assertEquals(outputs[1], false);
    test:assertEquals(outputs[2], "m1 is immutable: ");
    test:assertEquals(outputs[3], false);
    test:assertEquals(outputs[4], "m2 is immutable: ");
    test:assertEquals(outputs[5], true);
    test:assertEquals(outputs[6], "Error occurred on update: ");
    test:assertEquals(outputs[7], "Invalid map insertion: modification not allowed on readonly value");
    test:assertEquals(outputs[8], "m2 === m3: ");
    test:assertEquals(outputs[9], true);
    test:assertEquals(outputs[10], "frozenVal is map<string>");
}
