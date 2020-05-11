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

    string out1 = "Integer array size: ";
    string out2 = "JSON array size: ";
    string out3 = "Map size: ";
    string out4 = "String size: ";
    string out5 = "XML child elements size: ";
    string out6 = "Tuple size: ";
    string out7 = "Field size in `Student` record: ";
    test:assertEquals(outputs[0], out1);
    test:assertEquals(outputs[1], 3);
    test:assertEquals(outputs[2], out2);
    test:assertEquals(outputs[3], 2);
    test:assertEquals(outputs[4], out3);
    test:assertEquals(outputs[5], 3);
    test:assertEquals(outputs[6], out4);
    test:assertEquals(outputs[7], 23);
    test:assertEquals(outputs[8], out5);
    test:assertEquals(outputs[9], 2);
    test:assertEquals(outputs[10], out6);
    test:assertEquals(outputs[11], 2);
    test:assertEquals(outputs[12], out7);
    test:assertEquals(outputs[13], 4);
}
