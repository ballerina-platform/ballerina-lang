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
    foreach v in s {
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
    test:assertEquals(outputs[2], "frozen status of m1: ");
    test:assertEquals(outputs[3], true);
    test:assertEquals(outputs[4], "error occurred on update: ");
    test:assertEquals(outputs[5], "Invalid map insertion: modification not allowed on frozen value");
    test:assertEquals(outputs[6], "'freeze()' successful for m3");
    test:assertEquals(outputs[7], "'freeze()' failed for m4: ");
    //test:assertEquals(outputs[8], "'freeze()' not allowed on 'Employee'");
}
