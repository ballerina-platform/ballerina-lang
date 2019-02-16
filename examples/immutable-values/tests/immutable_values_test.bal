import ballerina/test;
import ballerina/io;

any[] outputs = [];
int counter = 0;

// This is the mock function which will replace the real function
@test:Mock {
    moduleName: "ballerina/io",
    functionName: "println"
}
public function mockPrint(any... s) {
    foreach var v in s {
        outputs[counter] = v;
        counter += 1;
    }
}

@test:Config
function testFunc() {
    // Invoking the main function
    main();
    test:assertEquals(outputs[0], "m1 === m2: ");
    test:assertEquals(outputs[1], true);
    test:assertEquals(outputs[2], "Frozen status of m1: ");
    test:assertEquals(outputs[3], true);
    test:assertEquals(outputs[4], "Error occurred on update: ");
    test:assertEquals(outputs[5], "Invalid map insertion: modification not allowed on frozen value");
    test:assertEquals(outputs[6], "'.freeze()' successful for m3");
    test:assertEquals(outputs[7], "'.freeze()' failed for m4: ");
    string output8 = <string> outputs[8];
    test:assertTrue(output8.hasPrefix("'freeze()' not allowed on '") && output8.hasSuffix("Employee'"));
    test:assertEquals(outputs[9], "frozenVal is map<string>");
}
