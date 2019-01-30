import ballerina/test;
import ballerina/log;
import ballerina/io;

string log = "";

// This is the mock function which will replace the real function
@test:Mock {
    moduleName: "ballerina/log",
    functionName: "printInfo"
}
public function mockPrintInfo(string|(function () returns (string)) msg) {
    if (msg is string) {
        log = msg;
    }
}

@test:Config
function testFunc() {
    // Invoking the main function
    main();
    test:assertTrue(log.contains("tasks#taskLists"));
}
