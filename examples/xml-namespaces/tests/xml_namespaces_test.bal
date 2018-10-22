import ballerina/test;
import ballerina/io;

any[] outputs = [];
int counter = 0;

// This is the mock function, which replaces the real function.
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
    // Invoking the main function. 
    main();
    string op1 = "{http://ballerina.com/aa}foo";
    string op2 = "{http://ballerina.com/updated}foo";

    test:assertEquals(outputs[0], op1);
    test:assertEquals(outputs[1], op2);
}
