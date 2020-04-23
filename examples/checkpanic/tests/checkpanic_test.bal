import ballerina/test;
import ballerina/io;

(any|error)[] outputs = [];
int counter = 0;

// This is the mock function that replaces the real function.
@test:Mock {
    moduleName: "ballerina/io",
    functionName: "println"
}
public function mockPrint(any|error... s) {
    foreach var val in s {
        outputs[counter] = val;
        counter += 1;
    }
}

@test:Config {}
function testFunc() {
    // Invoking the main function
    error? e = trap main();
    test:assertEquals(outputs[0], 120);
    test:assertTrue(e is error, msg = "expected main to panic");
    if (e is error) {
        test:assertEquals("{ballerina/lang.int}NumberParsingError", io:sprintf("%s", e.reason()));
    }
}
