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
    string out1 = "Child name : Raja and message : Hi Raja!!! , wish you a happy children's day";
    string out2 = "Child name : Shareek and message : Hi Shareek!!! , wish you a happy children's day";
    test:assertEquals(outputs.length(), 2);
    test:assertEquals(outputs[0], out1);
    test:assertEquals(outputs[1], out2);
}
