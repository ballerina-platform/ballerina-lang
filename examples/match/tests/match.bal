import ballerina/test;

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

@test:Config {}
function testFunc() {
    // Invoking the main function
    main();

    string out1 = "value is: 0";
    string out2 = "value is: 1";
    string out3 = "value is: 2";
    string out4 = "value is: 3";
    string out5 = "value is: 4";
    string out6 = "Cat";
    string out7 = "Dog";
    string out8 = "Mouse";
    string out9 = "Match All";


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
