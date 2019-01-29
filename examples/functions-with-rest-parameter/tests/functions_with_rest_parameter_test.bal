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
}

@test:Config
function testFunc() {
    // Invoking the main function
    main();
    test:assertEquals(outputs[0], "Fruits: ");
    test:assertEquals(outputs[1], "Fruits: Apples");
    test:assertEquals(outputs[2], "Available Fruits: Apples");
    test:assertEquals(outputs[3], "Fruits: Apples,Oranges");
    test:assertEquals(outputs[4], "Available Fruits: Apples,Oranges,Grapes");
    test:assertEquals(outputs[5], "Available Fruits: Apples,Oranges,Grapes");
    test:assertEquals(outputs[6], "Available Fruits: Apples,Oranges,Grapes");
}
