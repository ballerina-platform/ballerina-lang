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
    test:assertEquals(outputs[0], "Name: Peter");
    test:assertEquals(outputs[1], "Age: 28");
    test:assertEquals(outputs[2], "Other Details: {\"country\":\"Sri Lanka\", \"occupation\":\"Software Engineer\"}");
    test:assertEquals(outputs[3], "Name: Peter");
    test:assertEquals(outputs[4], "Age: 28");
    test:assertEquals(outputs[5], "Name: John");
    test:assertEquals(outputs[6], "Age: 26");
    test:assertEquals(outputs[7], "Country Name: Sri Lanka");
    test:assertEquals(outputs[8], "Capital Name: Colombo");
}
