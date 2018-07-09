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
    string outstr;
    foreach str in s{
        outstr = outstr + <string>str;
    }
    outputs[counter] = outstr;
    counter++;
}

@test:Config
function testFunc() {
    // Invoking the main function
    main();

    string out1 = "Start dividing numbers";
    string out2 = "Error occurred: Division by 0 is not defined";
    string out3 = "Finally block executed";
    test:assertEquals(out1, outputs[0]);
    test:assertEquals(out2, outputs[1]);
    test:assertEquals(out3, outputs[2]);
}
