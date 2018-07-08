import ballerina/test;
import ballerina/io;

any[] outputs = [];
int counter = 0;

// This is the mock function which will replace the real function
@test:Mock {
    packageName: "ballerina/io",
    functionName: "println"
}
public function mockPrint(any... s) {
    outputs[counter] = s[0];
    counter++;
}

@test:Config
function testFunc() {
    // Invoking the main function
    main();
    test:assertEquals(<string>outputs[0], "{\"name\":\"Jon\",\"age\":25,\"city\":\"Colombo\"}");
    test:assertEquals(<string>outputs[1], "Software Engineer");
}
