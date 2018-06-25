import ballerina/test;
import ballerina/io;

any[] outputs = [];
int count = 0;
// This is the mock function which will replace the real function
@test:Mock {
    packageName: "ballerina/io",
    functionName: "println"
}
public function mockPrint(any... s) {
    outputs[count] = s[0];
    count++;
}

@test:Config
function testFunc() {
    // Calling the main fuction with ampty args array
    main();
    test:assertEquals(5, outputs[0]);
    test:assertEquals(15, outputs[1]);
    int[] ia = [1, 3, 5, 6];
    test:assertEquals(ia, outputs[2]);
    test:assertEquals("cat", outputs[3]);
}
