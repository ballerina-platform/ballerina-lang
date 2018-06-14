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
    // The output is in random order
    foreach x in outputs {
        string value = <string>x;
        if (value.equalsIgnoreCase("Hello, World! #m")) {
            // continue;
        } else if (value.equalsIgnoreCase("Hello, World! #n")) {
            // continue;
        } else if (value.equalsIgnoreCase("Hello, World! #k")) {
            // continue;
        }
        else {
            test:assertFail(msg = "The output doesn't contain the expected.");
        }
    }
}
