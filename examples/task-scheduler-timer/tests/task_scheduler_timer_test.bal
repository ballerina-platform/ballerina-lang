import ballerina/io;
import ballerina/test;

(any|error)[] outputs = [];
int counter = 0;

// This is the mock function that replaces the real function.
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
    // Invoke the main function.
    main();
    test:assertEquals(outputs[0], "Hi Sam you are 1 years old now.");
    test:assertEquals(outputs[1], "Hi Sam you are 2 years old now.");
    test:assertEquals(outputs[2], "Hi Sam you are 3 years old now.");
    test:assertEquals(outputs[3], "Hi Sam you are 4 years old now.");
    test:assertEquals(outputs[4], "Hi Sam you are 5 years old now.");
    test:assertEquals(outputs[5], "Hi Sam you are 6 years old now.");
    test:assertEquals(outputs[6], "Sam started schooling");
    test:assertEquals(outputs[7], "Hi Sam you are 7 years old now.");
    test:assertEquals(outputs[8], "Hi Sam you are 8 years old now.");
    test:assertEquals(outputs[9], "Hi Sam you are 9 years old now.");
    test:assertEquals(outputs[10], "Hi Sam you are 10 years old now.");
    test:assertEquals(outputs[11], "End.");
}
