import ballerina/test;
import ballerina/http;

any[] outputs = [];
int counter = 0;

// This is the mock function which will replace the real function
@test:Mock {
    moduleName: "ballerina/log",
    functionName: "printInfo"
}
public function mockPrintInfo(string|(function () returns (string)) msg) {
    if (msg is string) {
        outputs[counter] = msg;
        counter += 1;
    }
}

@test:Config
function testFunc() {
    main();
    test:assertEquals(outputs[0], "Received a promise for /resource1");
    test:assertEquals(outputs[1], "Received a promise for /resource2");
    test:assertEquals(outputs[2], "Push promise for resource2 rejected");
    test:assertEquals(outputs[3], "Received a promise for /resource3");
    test:assertEquals(outputs[4], "Response : {\"response\":{\"name\":\"main resource\"}}");
    test:assertEquals(outputs[5], "Promised resource : {\"push\":{\"name\":\"resource1\"}}");
    test:assertEquals(outputs[6], "Promised resource : {\"push\":{\"name\":\"resource3\"}}");
}
