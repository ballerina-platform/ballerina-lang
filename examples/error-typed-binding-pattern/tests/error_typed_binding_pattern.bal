import ballerina/test;

(any|error)[] outputs = [];

// This is the mock function which will replace the real function
@test:Mock {
    moduleName: "ballerina/io",
    functionName: "println"
}
public function mockPrint(any|error... s) {
    foreach var entry in s {
        outputs.push(entry);
    }
}

@test:Config {}
function testFunc() {
    // Invoking the main function
    main();
    map<anydata|error> mp1 = {info: "Detail Msg", fatal: true};
    map<anydata|error> mp2 = {fatal: true};
    test:assertEquals(outputs[0], "Reason String: Sample Error");
    test:assertEquals(outputs[1], "Info: ");
    test:assertEquals(outputs[2], "Detail Msg");
    test:assertEquals(outputs[3], "Fatal: ");
    test:assertEquals(outputs[4], true);
    test:assertEquals(outputs[5], "Reason String: ");
    test:assertEquals(outputs[6], "Sample Error");
    test:assertEquals(outputs[7], "Detail Mapping: ");
    test:assertEquals(outputs[8], mp1);
    test:assertEquals(outputs[9], "Detail Mapping: ");
    test:assertEquals(outputs[10], mp2);
    test:assertEquals(outputs[11], "Detail Message: ");
    test:assertEquals(outputs[12], "Failed Message");
}
