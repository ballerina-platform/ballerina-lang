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

    string out1 = "Element type of the array is Int";
    string out2 = "Int array is stamped as JSON array";
    string out3 = "School of the Employee is Hindu College";
    string out4 = "Map is stamped as Teacher record type";
    string out5 = "Tuple is stamped as int array";
    string out5 = "JSON value is stamped as Foo";
    test:assertEquals(outputs[0], out1);
    test:assertEquals(outputs[1], out2);
    test:assertEquals(outputs[2], out3);
    test:assertEquals(outputs[3], out4);
    test:assertEquals(outputs[4], out5);
    test:assertEquals(outputs[5], out6);
}
