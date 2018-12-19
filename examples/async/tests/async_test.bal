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
    outputs[counter] = s[0];
    counter += 1;
}

@test:Config
function testFunc() {
    // Invoke the main function.
    main();
    //test:assertEquals(outputs[0], "SQ + CB = 737100");
    test:assertEquals(outputs[1], false);
    test:assertEquals(outputs[2], false);
    test:assertEquals(outputs[3], true);
    test:assertEquals(outputs[5], false);
    test:assertEquals(outputs[6], true);
    test:assertEquals(outputs[7], 100);
    test:assertEquals(outputs[8], false);

    var j9 = json.convert(outputs[9]);
    if (j9 is json) {
        test:assertEquals(j9.args.test, "123");
    }

    test:assertEquals(outputs[10], true);
    test:assertEquals(outputs[11], 400);

    var j12 = json.convert(outputs[12]);
    if (j12 is json) {
        test:assertEquals(j12.first_field, 100);
        test:assertEquals(j12.second_field, 27);
        runtime:sleep(2000);
        test:assertEquals(j12.third_field, "Hello Moose!!");
    }

    test:assertEquals(outputs[13], "first field of record --> 100");
    test:assertEquals(outputs[14], "second field of record --> 27");
    test:assertEquals(outputs[15], "third field of record --> Hello Moose!!");

}