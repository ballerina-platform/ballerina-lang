import ballerina/test;
import ballerina/log;
import ballerina/io;

string[] outputs = [];
int counter = 0;

// This is a mock function for `log:printInfo`, which will replace the real function.
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

// This is a mock function for `log:printError`, which will replace the real function.
@test:Mock {
    moduleName: "ballerina/log",
    functionName: "printError"
}
public function mockPrintError(string|(function() returns (string)) msg, error? err = ()) {
    if (msg is string) {
        outputs[counter] = msg;
        counter += 1;
    }
}

@test:Config
function testFunc() {
    // Invoking the main function
    main();
    test:assertTrue(outputs[0] == "{9356828d-6797-496f-975e-3fabaf677214}");
    test:assertTrue(outputs[1].contains("Failed to call the endpoint"));
    test:assertTrue(outputs[2] == "tasks#taskLists");
}
