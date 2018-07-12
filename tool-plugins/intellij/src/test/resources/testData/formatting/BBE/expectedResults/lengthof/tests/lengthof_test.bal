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

    string out1 = "Integer array size: 3";
    string out2 = "JSON array size: 2";
    string out3 = "Map size: 2";
    string out4 = "String size: 23";
    string out5 = "XML child elements size: 2";
    test:assertEquals(out1, outputs[0]);
    test:assertEquals(out2, outputs[1]);
    test:assertEquals(out3, outputs[2]);
    test:assertEquals(out4, outputs[3]);
    test:assertEquals(out5, outputs[4]);
}
