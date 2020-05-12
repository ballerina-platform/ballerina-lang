import ballerina/test;

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
        outstr = outstr + <string>str;
    }
    outputs[counter] = outstr;
    counter += 1;
}

@test:Config {}
function testFunc() {
    // Invoking the main function
    main();
    test:assertEquals(outputs[0], "key1: value1");
    test:assertEquals(outputs[1], "keys: [key2]");
}
