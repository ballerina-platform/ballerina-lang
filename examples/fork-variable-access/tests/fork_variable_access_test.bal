import ballerina/io;
import ballerina/runtime;
import ballerina/test;

(string|error)[] outputs = [];
int counter = 0;

// This is the mock function that replaces the real function.
@test:Mock {
    moduleName: "ballerina/io",
    functionName: "println"
}
public function mockPrint(any|error... s) {
    string outStr = "";
    foreach var str in s {
        outStr = outStr + string.convert(str);
    }
    lock {
        outputs[counter] = outStr;
        counter += 1;
    }
}

@test:Config
function testFunc() {
    // Invoking the main function
    main();

    // Retry for 10 times and wait for all workers to finish
    int i = 0;
    while (i <= 10) {
        if (outputs.length() < 4) {
            i += i;
            // Sleep for 50 milli seconds
            runtime:sleep(50);
            continue;
        } else {
            break;
        }
    }

    if (outputs.length() < 4) {
        test:assertFail(msg = "The output array doesn't contain the expected number of elements.");
    } else {
        // The output is in random order
        foreach var x in outputs {
            string value = string.convert(x);
            if (value.equalsIgnoreCase("[value type variables] before fork: value of integer variable is [100] value of string variable is [WSO2]")) {
                // continue;
            } else if (value.equalsIgnoreCase("[reference type variables] before fork: value of name is [Bert] value of city is [New York] value of postcode is [10001]")) {
                // continue;
            } else if (value.equalsIgnoreCase("[value type variables] after fork: value of integer variable is [123] value of string variable is [Ballerina]")) {
                // continue;
            } else if (value.equalsIgnoreCase("[reference type variables] after fork: value of name is [Moose] value of city is [Manhattan] value of street is [Wall Street] value of postcode is [10001]")) {
                // continue;
            } else {
                io:println(x);
                test:assertFail(msg = "The output doesn't contain the expected.");
            }
        }
    }
}
