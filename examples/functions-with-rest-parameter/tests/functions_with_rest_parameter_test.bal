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
        outstr = outstr + <string>str;
    }
    outputs[counter] = outstr;
    counter += 1;
}

@test:Config {}
function testFunc() {
    // Invoking the main function
    main();
    test:assertEquals(outputs[0], "Name: Alice, Age: 18");
    test:assertEquals(outputs[1], "Name: Bob, Age: 20");
    test:assertEquals(outputs[2], "Name: Corey, Age: 19, Module(s): Math");
    test:assertEquals(outputs[3], "Name: Diana, Age: 20, Module(s): Math, Physics");
    test:assertEquals(outputs[4], "Name: Diana, Age: 20, Module(s): Math, Physics");
}
