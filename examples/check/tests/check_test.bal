import ballerina/test;

(any|error)[] outputs = [];
int counter = 0;

// This is the mock function that replaces the real function.
@test:Mock {
    moduleName: "ballerina/io",
    functionName: "println"
}
public function mockPrint(any|error... s) {

    foreach any|error a in s {
        if (a is error) {
            outputs[counter] = a;
        } else {
            outputs[counter] = a;
        }
        counter += 1;
    }
}

@test:Config {}
function testFunc() {
    // call the main function
    main();
    test:assertEquals(outputs[0], 12);
    test:assertEquals(outputs[1].toString(), "error {ballerina/lang.int}NumberParsingError message='string' value 'invalid' cannot be converted to 'int'");
    test:assertEquals(outputs[2], 120);
    test:assertEquals(outputs[3].toString(), "error {ballerina/lang.int}NumberParsingError message='string' value 'Invalid' cannot be converted to 'int'");
}
