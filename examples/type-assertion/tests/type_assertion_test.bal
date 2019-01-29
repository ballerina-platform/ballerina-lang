import ballerina/test;
import ballerina/io;

any[] outputs = [];
int counter = 0;

// This is the mock function that replaces the real function.
@test:Mock {
    moduleName: "ballerina/io",
    functionName: "println"
}
public function mockPrint(any... s) {
    foreach var elem in s {
        outputs[counter] = elem;
        counter += 1;
    }
}

@test:Config
function testFunc() {
    // Invoking the main function
    main();
    test:assertEquals(outputs[0], "asserted employee's name: ");
    test:assertEquals(outputs[1], "Speedy Gonzales");
    any a = outputs[2];
    if (a is string) {
        test:assertTrue(a.hasPrefix("assertion error: "));
    } else {
        test:assertFail(msg = "expected value to be of type string");
    }
}
