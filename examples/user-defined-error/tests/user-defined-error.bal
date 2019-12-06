import ballerina/test;
import ballerina/io;

# todo: add correct tests

any[] outputs = [];
int counter = 0;

// This is the mock function that replaces the real function.
@test:Mock {
    moduleName: "ballerina/io",
    functionName: "println"
}
public function mockPrint(any... s) {
    foreach var entry in s {
        outputs[counter] = entry;
        counter += 1;
    }
}

@test:Config
function testFunc() {
    // Invoke the main function.
    main();
    test:assertEquals(outputs[0], "Error: ");
    test:assertEquals(outputs[1], "InvalidAccountType");
    test:assertEquals(outputs[2], ", Account type: ");
    test:assertEquals(outputs[3], "Joined");
    test:assertEquals(outputs[4], "Error: ");
    test:assertEquals(outputs[5], "InvalidAccountID");
    test:assertEquals(outputs[6], ", Account ID: ");
    test:assertEquals(outputs[7], -1);
    test:assertEquals(outputs[8], "Error: ");
    test:assertEquals(outputs[9], "AccountInquiryFailed, ");
    test:assertEquals(outputs[10], "Message: ");
}
