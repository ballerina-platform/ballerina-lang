import ballerina/io;
import ballerina/test;

any[] outputs = [];
int counter = 0;

// This is the mock function which will replace the real function
@test:Mock {
    moduleName: "ballerina/io",
    functionName: "println"
}
public function mockPrint(any... s) {
    outputs[counter] = s[0];
    counter += 1;
}

@test:Config
function testFunc() {
    // Invoking the main function
    main();
    test:assertEquals(outputs[0], "Reason String: Sample Error");
    test:assertEquals(outputs[1], "Detail Mapping: {\"detail\":\"Detail Msg\", \"fatal\":true}");
    test:assertEquals(outputs[2], "Reason String: Sample Error");
    test:assertEquals(outputs[3], "Detail Mapping Field One: Detail Msg");
    test:assertEquals(outputs[4], "Detail Mapping Field Two: true");
    test:assertEquals(outputs[5], "Reason String: Sample Error");
    test:assertEquals(outputs[6], "Detail Mapping: {\"detail\":\"Detail Msg\", \"fatal\":true}");
    test:assertEquals(outputs[7], "Detail Mapping: {detailMsg:\"Failed Message\", isFatal:true}");
}
