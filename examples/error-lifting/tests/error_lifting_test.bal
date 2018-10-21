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
    outputs[counter] = s[1];
    counter += 1;
}

@test:Config
function testFunc() {
    // Invoking the main function
    main();

    string statusCode = "The status code: ";
    error err = error("response error");
    test:assertEquals(outputs[0], statusCode);
    test:assertEquals(outputs[1], err);
    test:assertEquals(outputs[2], statusCode);
    test:assertEquals(outputs[3], ());
}
