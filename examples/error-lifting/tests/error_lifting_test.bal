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
    outputs[counter] = s[1];
    counter++;
}

@test:Config
function testFunc() {
    // Invoking the main function
    main();

    string statusCode = "The status code: ";
    error err = { message: "response error" };
    test:assertEquals(statusCode, outputs[0]);
    test:assertEquals(err, outputs[1]);
    test:assertEquals(statusCode, outputs[2]);
    test:assertEquals((), outputs[3]);
}
