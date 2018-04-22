import ballerina/test;
import ballerina/io;

any [] outputs = [];
int counter = 0;
 // This is the mock function which will replace the real function
@test:Mock {
    packageName : "ballerina.io" ,
    functionName : "println"
}
public function mockPrint (any... s) {
    outputs[counter] = s[0];
    counter++;
}

@test:Config
function testFunc() {
    // Invoking the main function
    main();
    test:assertEquals("Employee event received for Employee Name: Jane", outputs[0]);
    test:assertEquals("Employee event received for Employee Name: Anne", outputs[1]);
    test:assertEquals("Employee event received for Employee Name: John", outputs[2]);
    test:assertEquals("Temperature event received: 28.0", outputs[3]);
    test:assertEquals("Temperature event received: 30.1", outputs[4]);
    test:assertEquals("Temperature event received: 29.5", outputs[5]);
    test:assertEquals("Event received: Hello Ballerina!", outputs[6]);
    test:assertEquals("Event received: 1.0", outputs[7]);
    test:assertEquals("Event received: {id:1, name:\"Jane\"}", outputs[8]);
}
