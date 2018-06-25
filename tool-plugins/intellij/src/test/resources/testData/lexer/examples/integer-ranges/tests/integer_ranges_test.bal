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
    outputs[counter] = s[1];
    counter++;
}

@test:Config
function testFunc() {
    // Invoking the main function
    main();

    test:assertEquals("Length of the array created by the closed integer range 25 ... 28: ", outputs[0]);
    test:assertEquals(4, outputs[1]);
    test:assertEquals("First element: ", outputs[2]);
    test:assertEquals(25, outputs[3]);
    test:assertEquals("Last element: ", outputs[4]);
    test:assertEquals(28, outputs[5]);
    test:assertEquals("\nLength of the array created by the half open integer range 25 ..< 28: ", outputs[6]);
    test:assertEquals(3, outputs[7]);
    test:assertEquals("First element: ", outputs[8]);
    test:assertEquals(25, outputs[9]);
    test:assertEquals("Last element: ", outputs[10]);
    test:assertEquals(27, outputs[11]);
}
