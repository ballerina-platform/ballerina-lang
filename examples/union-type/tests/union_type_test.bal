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
    outputs[counter] = s[0];
    counter++;
}

@test:Config
function testFunc() {
    // Invoking the main function
    main();
    test:assertEquals("This is a string", outputs[0]);
    test:assertEquals(101, outputs[1]);
    test:assertEquals("this is a value", outputs[2]);

    //ExpectedRecordType expectedError = {message: "key '' not found", cause: null, key: ""};
    //test:assertEquals(expectedError, outputs[3]);
}

type ExpectedRecordType record {
    string message;
    error? cause;
    string key;
};
