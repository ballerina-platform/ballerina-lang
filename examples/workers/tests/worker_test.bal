import ballerina/test;
import ballerina/io;

any[] outputs = [];
int counter = 0;

// This is the mock function that replaces the real function.
@test:Mock {
    packageName: "ballerina/io",
    functionName: "println"
}
public function mockPrint(any... s) {
    string outStr;
    foreach str in s{
        outStr = outStr + <string>str;
    }
    outputs[counter] = outStr;
    counter++;
}

@test:Config
function testFunc() {
    // Invoke the main function. 
    main();
    test:assertEquals("sum of first 10000000 positive numbers = 50000005000000", outputs[0]);
    test:assertEquals("sum of squares of first 10000000 positive numbers = 1291990006563070912", outputs[1]);
}
