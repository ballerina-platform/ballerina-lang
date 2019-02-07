import ballerina/test;
import ballerina/io;
import ballerina/runtime;

any[] outputs = [];
int counter = 0;

// This is the mock function that replaces the real function.
@test:Mock {
    moduleName: "ballerina/io",
    functionName: "println"
}

public function mockPrint(any... s) {
    lock{
        outputs[counter] = s[0];
        counter += 1;
    }
}

@test:Config
function testFunc() {
    // Invoke the main function.
    main();
    // A `runtime:sleep` is added to delay the execution.
    runtime:sleep(2000);
    test:assertEquals(outputs[1], false);
    test:assertEquals(outputs[2], false);
    test:assertEquals(outputs[3], true);
    test:assertEquals(outputs[5], false);

    var j9 = json.convert(outputs[6]);
    if (j9 is json) {
        test:assertEquals(j9.args.test, "123");
    }

    test:assertEquals(outputs[7], true);
    test:assertEquals(outputs[8], 400);

    var j12 = json.convert(outputs[9]);
    if (j12 is json) {
        test:assertEquals(j12.first_field, 100);
        test:assertEquals(j12.second_field, 27);
        // A `runtime:sleep` is added to delay the execution.
        runtime:sleep(2000);
        test:assertEquals(j12.third_field, "Hello Moose!!");
    }

    test:assertEquals(outputs[10], "first field of record --> 100");
    test:assertEquals(outputs[11], "second field of record --> 27");
    test:assertEquals(outputs[12], "third field of record --> Hello Moose!!");

}