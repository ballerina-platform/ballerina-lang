import ballerina/test;
import ballerina/io;

any[] outputs = [];
int count = 0;

// This is the mock function, which will replace the real function.
@test:Mock {
    moduleName: "ballerina/io",
    functionName: "println"
}
public function mockPrint(any... s) {
    outputs[count] = s[0];
    count += 1;
}

@test:Config
function testFunc() {
    // Calling the main function with an empty args array.
    main();
    test:assertEquals(outputs[0], "GET action");
    test:assertEquals(outputs[1], "POST action");
    test:assertEquals(outputs[2], 135);

    map<string> output1 = {  "user": "Ballerina", "ID": "1234" };
    test:assertEquals(outputs[3], output1);

    map<map<string>> output2 = { "data": output1, "data2": { "user": "WSO2" } };
    test:assertEquals(outputs[4], output2);

    test:assertEquals(outputs[5], "Ballerina");
}
