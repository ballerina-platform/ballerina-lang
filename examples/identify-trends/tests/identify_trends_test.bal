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
    string out1 = "printInitalAndPeakTemp function is invoked. InitialTemp : 20.0 and Peak temp : 23.0";
    string out2 = "printInitalAndPeakTemp function is invoked. InitialTemp : 21.0 and Peak temp : 24.0";
    test:assertEquals(lengthof outputs, 2);
    test:assertEquals(outputs[0], out1);
    test:assertEquals(outputs[1], out2);
}
