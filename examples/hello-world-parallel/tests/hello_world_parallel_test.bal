import ballerina/runtime;
import ballerina/test;

string[] outputs = [];
int counter = 0;

// This is the mock function that replaces the real function.
@test:Mock {
    moduleName: "ballerina/io",
    functionName: "println"
}
public function mockPrint(any... s) {
    string outStr = "";
    foreach var str in s {
        outStr = outStr + str.toString();
    }
    lock {
        outputs[counter] = outStr;
        counter += 1;
    }
}

@test:Config {}
function testFunc() {
    // Invoking the main function
    main();

    // Retry for 10 times and wait for all workers to finish
    int i = 0;
    while (i <= 10) {
        if (outputs.length() < 3) {
            i += i;
            // Sleep for 50 milli seconds
            runtime:sleep(50);
            continue;
        } else {
            break;
        }
    }

    if (outputs.length() < 3) {
        test:assertFail(msg = "The output array doesn't contain the expected number of elements.");
    } else {
        // The output is in random order
        foreach var x in outputs {
            string value = x.toString();
            if (value == "Hello, World! #m") {
                // continue;
            } else if (value == "Hello, World! #n") {
                // continue;
            } else if (value == "Hello, World! #k") {
                // continue;
            } else {
                test:assertFail(msg = "The output doesn't contain the expected.");
            }
        }
    }
}
