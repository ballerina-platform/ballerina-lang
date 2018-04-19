import ballerina/test;
import ballerina/io;

any [] outputs = [];
int counter = 0;
 // This is the mock function which will replace the real function
@test:Mock {
    packageName : "ballerina.io" ,
    functionName : "println"
}
public function mockPrint (any s) {
    outputs[counter] = s;
    counter++;
}

@test:Config
function testFunc() {
    // Invoking the main function
    main();
    test:assertEquals(737100, outputs[0]);
    test:assertEquals(false, outputs[1]);
    test:assertEquals(false, outputs[2]);
    test:assertEquals(true, outputs[3]);
    //test:assertEquals(16810815, a[4]);
    test:assertEquals(true, outputs[5]);
    test:assertEquals(true, outputs[6]);
    test:assertEquals(100, outputs[7]);
    test:assertEquals(false, outputs[8]);
    json j = check <json>outputs[10];
    json j2 = {"test":"123"};
    //test:assertTrue(j.args == j2);
    test:assertEquals(true, outputs[11]);
}
