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
    test:assertEquals(outputs[0], "This is a string");
    test:assertEquals(outputs[1], 101);
    test:assertEquals(outputs[2], "this is a value");

    //ExpectedRecordType expectedError = {message: "key '' not found", cause: null, key: ""};
    //test:assertEquals(outputs[3], expectedError);
}

type ExpectedRecordType record {
    string message;
    error? cause;
    string key;
};
