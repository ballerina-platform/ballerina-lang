import ballerina/test;
import ballerina/io;

any[] outputs = [];
int counter = 0;

// This is the mock function that replaces the real function.
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
    // Invoke the main function.
    main();
    string out1 = "Child detected. Child name : Raja, age : 12 and from : Mountain View";
    string out2 = "Child detected. Child name : Shareek, age : 16 and from : Houston";
    test:assertEquals(outputs.length(), 2);
    test:assertEquals(outputs[0], out1);
    test:assertEquals(outputs[1], out2);
}
