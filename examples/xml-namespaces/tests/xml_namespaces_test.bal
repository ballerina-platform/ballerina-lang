import ballerina/test;
import ballerina/io;

any[] outputs = [];
int counter = 0;

// This is the mock function, which replaces the real function.
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
    // Invoking the main function. 
    main();
    string op1 = "{http://ballerina.com/aa}foo";
    string op2 = "{http://ballerina.com/updated}foo";

    test:assertEquals(op1, outputs[0]);
    test:assertEquals(op2, outputs[1]);
}
