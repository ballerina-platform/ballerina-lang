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
    string outstr;
    foreach str in s{
        outstr = outstr + <string>str;
    }
    outputs[counter] = outstr;
    counter += 1;
}

@test:Config
function testFunc() {
    // Invoking the main function
    main();

    string out1 = "Integer array size: 3";
    string out2 = "JSON array size: 2";
    string out3 = "Map size: 3";
    string out4 = "String size: 23";
    string out5 = "XML child elements size: 2";
    test:assertEquals(outputs[0], out1);
    test:assertEquals(outputs[1], out2);
    test:assertEquals(outputs[2], out3);
    test:assertEquals(outputs[3], out4);
    test:assertEquals(outputs[4], out5);
}
