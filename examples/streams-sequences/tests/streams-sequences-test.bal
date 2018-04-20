import ballerina/test;
import ballerina/io;

any [] outputs = [];
int counter = 0;
 // This is the mock function which will replace the real function
@test:Mock {
    packageName : "ballerina.io" ,
    functionName : "println"
}
public function mockPrint (any s) {
    outputs[counter] = s;
    counter++;
}

@test:Config
function testFunc() {
    // Invoking the main function
    main();
    string out1 = "printInitalAndPeakTemp function is invoked. InitialTemp:20.0 and Peak temp :23.0";
    string out2 = "printInitalAndPeakTemp function is invoked. InitialTemp:21.0 and Peak temp :24.0";
    test:assertEquals(out1, outputs[0]);
    test:assertEquals(out2, outputs[1]);
}
