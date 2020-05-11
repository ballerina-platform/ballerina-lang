import ballerina/test;

(any|error)[] outputs = [];
int counter = 0;

// This is the mock function which will replace the real function.
@test:Mock {
    moduleName: "ballerina/io",
    functionName: "println"
}
public function mockPrint(any|error... s) {
    foreach any a in s {
        outputs[counter] = a;
        counter += 1;
    }
}

@test:Config {}
function testFunc() {
    // Invoke the main function.
    main();
    test:assertEquals(outputs.length(), 18);
    test:assertEquals(outputs[0], "Number of elements in 'words': ");
    test:assertEquals(outputs[1], 5);
    test:assertEquals(outputs[2], <string[]>["ANT", "BEAR", "CAT", "DEAR", "ELEPHANT"]);
    test:assertEquals(outputs[3], "Average of positive numbers: ");
    test:assertEquals(outputs[4], 7.0);
    test:assertEquals(outputs[5], "\nExecution Order:-");
    test:assertEquals(outputs[6], "- map operation's value: ");
    test:assertEquals(outputs[7], "apple");
    test:assertEquals(outputs[8], "-- foreach operation's value: ");
    test:assertEquals(outputs[9], "apple");
    test:assertEquals(outputs[10], "- map operation's value: ");
    test:assertEquals(outputs[11], "[\"red\", \"green\"]");
    test:assertEquals(outputs[12], "-- foreach operation's value: ");
    test:assertEquals(outputs[13], "[\"red\", \"green\"]");
    test:assertEquals(outputs[14], "- map operation's value: ");
    test:assertEquals(outputs[15], "5");
    test:assertEquals(outputs[16], "-- foreach operation's value: ");
    test:assertEquals(outputs[17], "5");
}
