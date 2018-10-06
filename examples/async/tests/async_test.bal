import ballerina/test;
import ballerina/io;

any[] outputs = [];
int counter = 0;

// This is the mock function that replaces the real function. 
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
    // Invoke the main function.
    main();
    //TODO fix following commented assertions
    //test:assertEquals("737100", outputs[0]);
    test:assertEquals(outputs[1], false);
    test:assertEquals(outputs[2], false);
    test:assertEquals(outputs[3], true);
    //test:assertEquals(16810815, a[4]);
    test:assertEquals(outputs[5], true);
    test:assertEquals(outputs[6], true);
    test:assertEquals(outputs[7], 100);
    test:assertEquals(outputs[8], false);
    json j = check <json>outputs[9];
    json j2 = {"test":"123"};
    test:assertEquals(j.args, j2);
    test:assertEquals(true, outputs[10]);
}
