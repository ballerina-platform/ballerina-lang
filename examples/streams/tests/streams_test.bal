import ballerina/test;
import ballerina/io;

any[] outputs = [];
int counter = 0;

// This is the mock function that replaces the real function. 
@test:Mock {
    moduleName: "ballerina/io",
    functionName: "println"
}
public function mockPrint(any... s) {
    outputs[counter] = s[0];
    if (counter > 5) {
        counter += 1;
        outputs[counter] = s[1];
    }
    counter += 1;
}

@test:Config
function testFunc() {
    // Invoke the main function.  
    main();
    test:assertEquals(outputs[0], "Employee event received for Employee Name: Jane");
    test:assertEquals(outputs[1], "Employee event received for Employee Name: Anne");
    test:assertEquals(outputs[2], "Employee event received for Employee Name: John");
    test:assertEquals(outputs[3], "Temperature event received: 28.0");
    test:assertEquals(outputs[4], "Temperature event received: 30.1");
    test:assertEquals(outputs[5], "Temperature event received: 29.5");
    test:assertEquals(outputs[6], "Event received: ");
    test:assertEquals(outputs[7], "Hello Ballerina!");
    test:assertEquals(outputs[8], "Event received: ");
    test:assertEquals(outputs[9], 1.0);
    test:assertEquals(outputs[10], "Event received: ");
    Employee e = { id: 1, name: "Jane" };
    test:assertEquals(outputs[11], e);
}
