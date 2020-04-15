import ballerina/test;

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

@test:Config {}
function testFunc() {
    // Calling the main fuction with ampty args array
    main();
    test:assertEquals(outputs[0], 5);
    test:assertEquals(outputs[1], 15);
    test:assertEquals(outputs[2], 25);
    int[] ia = [1, 3, 5, 6];
    test:assertEquals(outputs[3], ia);
    test:assertEquals(outputs[4], "cat");
}
