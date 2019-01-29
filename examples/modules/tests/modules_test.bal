import ballerina/test;
import ballerina/io;

any output = "";

// This is the mock function which will replace the real function
@test:Mock {
    moduleName: "ballerina/io",
    functionName: "println"
}
public function mockPrint(any... s) {
    output = s[0];
}

@test:Config
function testFunc() {
    // Invoking the main function
    main();

    float expected = 3.141592653589793;
    test:assertEquals(output, expected);
}
