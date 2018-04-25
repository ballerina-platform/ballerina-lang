import ballerina/test;
import ballerina/io;

any[] outputs = [];
int counter = 0;

// This is the mock function which will replace the real function
@test:Mock {
    packageName: "ballerina.io",
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

    string out1 = "Integer array size: 3";
    string out2 = "JSON array size: 2";
    string out3 = "JSON array size: 2";
    string out4 = "Map size: 2";
    string out5 = "String size: 23";
    string out6 = "XML child elements size: 2";
    string out7 = "BLOB size: 23";
    test:assertEquals(out1, outputs[0]);
    test:assertEquals(out2, outputs[1]);
    test:assertEquals(out3, outputs[2]);
    test:assertEquals(out4, outputs[3]);
    test:assertEquals(out5, outputs[4]);
    test:assertEquals(out6, outputs[5]);
    test:assertEquals(out7, outputs[6]);
}
