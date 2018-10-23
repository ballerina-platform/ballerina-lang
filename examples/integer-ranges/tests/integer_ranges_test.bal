import ballerina/test;
import ballerina/io;

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
    outputs[counter] = s[1];
    counter += 1;
}

@test:Config
function testFunc() {
    // Invoking the main function
    main();

    test:assertEquals(outputs[0], "Length of the array created by the closed integer range 25 ... 28: ");
    test:assertEquals(outputs[1], 4);
    test:assertEquals(outputs[2], "First element: ");
    test:assertEquals(outputs[3], 25);
    test:assertEquals(outputs[4], "Last element: ");
    test:assertEquals(outputs[5], 28);
    test:assertEquals(outputs[6], "\nLength of the array created by the half open integer range 25 ..< 28: ");
    test:assertEquals(outputs[7], 3);
    test:assertEquals(outputs[8], "First element: ");
    test:assertEquals(outputs[9], 25);
    test:assertEquals(outputs[10], "Last element: ");
    test:assertEquals(outputs[11], 27);
}
