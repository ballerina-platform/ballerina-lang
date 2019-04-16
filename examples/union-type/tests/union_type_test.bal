import ballerina/test;
import ballerina/io;

(any|error)?[] outputs = [];
int counter = 0;

// This is the mock function which will replace the real function
@test:Mock {
    moduleName: "ballerina/io",
    functionName: "println"
}
public function mockPrint(any|error... s) {
    outputs[counter] = s[0];
    counter += 1;
}

@test:Config
function testFunc() {
    // Invoking the main function
    main();
    test:assertEquals(<string> outputs[0], "This is a string");
    test:assertEquals(<int> outputs[1], 101);
    test:assertEquals(<string> outputs[2], "this is a value");

    var output3 = outputs[3];
    if (output3 is error) {
        test:assertEquals(output3.reason(), "key '' not found");
    } else {
        test:assertFail();
    }
}
