import ballerina/test;
import ballerina/io;

string [] outputs = [];
int counter = 0;

// This is the mock function that replaces the real function.
@test:Mock {
    moduleName: "ballerina/io",
    functionName: "println"
}
public function mockPrint(any... s) {
    string outStr = "";
    foreach var str in s {
        outStr = outStr + <string> str;
    }
    lock {
        outputs[counter] = outStr;
        counter += 1;
    }
}

@test:Config
function testFunc() {
    // Invoke the main function.
    main();
    test:assertEquals(outputs[2], "[main] iW1: 23 sW1: Colombo fW2: 10.344");
}
