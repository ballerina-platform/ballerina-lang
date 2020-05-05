import ballerina/test;
import ballerina/io;

(any|error)[] outputs = [];
int counter = 0;

// This is the mock function which will replace the real function
@test:Mock {
    moduleName: "ballerina/io",
    functionName: "println"
}
public function mockPrint(any|error... s) {
    string outstr = "";
    foreach var str in s {
        outstr = outstr + io:sprintf("%s", str);
    }
    outputs[counter] = outstr;
    counter += 1;
}

@test:Config
function testFunc() {
    // Invoking the main function
    main();

    string out1 = "Matched with single var : 66.6";
    string out2 = "Matched with two vars : (\"Hello\", 12)";
    string out3 = "Matched with two vars : (4.5, true)";
    string out4 = "Matched with three vars : (6.7, \"Test\", false)";
    string out5 = "'s' is string and 'i' is int : (\"Hello\", 45)";
    string out6 = "Only 's' is float : (4.5, true)";
    string out7 = "Only 'i' is int : (false, 4)";
    string out8 = "No type guard : (455, true)";
    string out9 = "'s' is float only : 5.6";

    test:assertEquals(outputs[0], out1);
    test:assertEquals(outputs[1], out2);
    test:assertEquals(outputs[2], out3);
    test:assertEquals(outputs[3], out4);
    test:assertEquals(outputs[4], out5);
    test:assertEquals(outputs[5], out6);
    test:assertEquals(outputs[6], out7);
    test:assertEquals(outputs[7], out8);
    test:assertEquals(outputs[8], out9);
}
