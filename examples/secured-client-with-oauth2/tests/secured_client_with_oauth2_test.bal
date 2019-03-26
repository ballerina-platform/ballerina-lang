import ballerina/test;
import ballerina/log;
import ballerina/io;

string[] outputs = [];
int counter = 0;

// This is the mock function, which will replace the real function.
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
    // Invoking the main function
    main();
    test:assertTrue(outputs[0] == "{9356828d-6797-496f-975e-3fabaf677214}");
    test:assertTrue(outputs[1] == "{9356828d-6797-496f-975e-3fabaf677214}");
    test:assertTrue(outputs[2] == "tasks#taskLists");
}
