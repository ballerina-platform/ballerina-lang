import ballerina/test;

any[] outputs = [];
int counter = 0;

// This is the mock function which will replace the real function
@test:Mock {
    moduleName: "ballerina/io",
    functionName: "println"
}
public function mockPrint(any... s) {
    string outstr = "";
    foreach var str in s {
        outstr = outstr + str.toString();
    }
    outputs[counter] = outstr;
    counter += 1;
}

@test:Config {}
function testFunc() {
    // Invoking the main function
    main();

    string out1 = "Matched with two vars : Hello, 150";
    string out2 = "Matched with two vars : Hello, true";
    string out3 = "Matched with three vars : Hello, 150, true";
    string out4 = "Matched with single var : Hello";
    string out5 = "Matched with string and int typeguard : Ballerina";
    string out6 = "Matched with string and RecordTwo typeguard : Ballerina";
    string out7 = "Matched with Default";

    test:assertEquals(outputs[0], out1);
    test:assertEquals(outputs[1], out2);
    test:assertEquals(outputs[2], out3);
    test:assertEquals(outputs[3], out4);
    test:assertEquals(outputs[4], out5);
    test:assertEquals(outputs[5], out6);
    test:assertEquals(outputs[6], out7);
}
