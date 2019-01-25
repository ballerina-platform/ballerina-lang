import ballerina/test;
import ballerina/io;

any[] outputs = [];
int count = 0;
// This is the mock function which will replace the real function
@test:Mock {
    moduleName: "ballerina/io",
    functionName: "println"
}
public function mockPrint(any... s) {
    outputs[count] = s[0];
    count += 1;
}

@test:Config
function testFunc() {
    // Calling the main fuction with ampty args array
    main();
    test:assertEquals(outputs[0], "GET action");
    test:assertEquals(outputs[1], "POST action");
    test:assertEquals(outputs[2], 135);
}
