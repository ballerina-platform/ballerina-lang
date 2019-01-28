import ballerina/test;
import ballerina/io;

any[] outputs = [];
int count = 0;

@test:Mock {
    moduleName: "ballerina/io",
    functionName: "println"
}
public function mockPrint(any... s) {
    outputs[count] = string.convert(s[0]);
    count += 1;
}

@test:Config
function testFunc() {
    main();
    test:assertEquals(outputs[0], "Invoke local participant function.");
    test:assertEquals(outputs[1], "Local participant panicked.");
    test:assertEquals(outputs[2], "Retrying transaction");
    test:assertEquals(outputs[3], "Invoke local participant function.");
    test:assertEquals(outputs[4], "Local participant panicked.");
    test:assertEquals(outputs[5], "Retrying transaction");
    test:assertEquals(outputs[6], "Invoke local participant function.");
    test:assertEquals(outputs[7], "Local participant panicked.");
    test:assertEquals(outputs[8], "Transaction aborted");
}
