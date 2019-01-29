import ballerina/test;
import ballerina/io;

(any|error)[] outputs = [];
int counter = 0;

// This is the mock function which will replace the real function
@test:Mock {
    moduleName: "ballerina/io",
    functionName: "println"
}
public function mockPrint(any... s) {
    outputs[counter] = s[0];
    counter += 1;
    outputs[counter] = s[1];
    counter += 1;
}

@test:Config
function testFunc() {
    // Invoking the main function
    main();

    string statusCode = "The status code: ";
    test:assertEquals(outputs[0], statusCode);
    any|error res = outputs[1];
    if (res is error) {
        test:assertEquals(res.reason(), "response error");
    } else {
        test:assertFail(msg = "expected an error");
    }
    test:assertEquals(outputs[2], statusCode);
    test:assertEquals(outputs[3], ());
}
