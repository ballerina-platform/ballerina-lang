import ballerina/test;
import ballerina/io;

any[] outputs = [];
int counter = 0;

// This is the mock function which will replace the real function
@test:Mock {
    packageName: "ballerina/io",
    functionName: "println"
}
public function mockPrint(any... s) {
    outputs[counter] = s[0];
    counter++;
}

@test:Config
function testFunc() {
    // Invoking the main function
    main();
    //test:assertEquals(outputs[0], "[10, John]");
    test:assertEquals(outputs[1], 10);
    test:assertEquals(outputs[2], "John");
    test:assertEquals("06/10: quotient=0 remainder=6", outputs[3]);
    test:assertEquals("57/10: quotient=5", outputs[4]);
    test:assertEquals("09/10: remainder=9", outputs[5]);
}
