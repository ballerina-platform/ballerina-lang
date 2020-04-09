import ballerina/test;

any[] outputs = [];
int counter = 0;

// This is the mock function which will replace the real function
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
    // Invoking the main function
    main();
   
    test:assertEquals(outputs[0], "foreach for 25 ... 28");
    test:assertEquals(outputs[1], 25);
    test:assertEquals(outputs[2], 26);
    test:assertEquals(outputs[3], 27);
    test:assertEquals(outputs[4], 28);
    test:assertEquals(outputs[5], "\nforeach for 25 ..< 28");
    test:assertEquals(outputs[6], 25);
    test:assertEquals(outputs[7], 26);
    test:assertEquals(outputs[8], 27);
    test:assertEquals(outputs[9], "\niterable object for 25 ..< 28");
    test:assertEquals(outputs[10], 25);
    test:assertEquals(outputs[11], 26);
    test:assertEquals(outputs[12], 27);
}
