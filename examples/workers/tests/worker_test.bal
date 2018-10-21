import ballerina/test;
import ballerina/io;

any[] outputs = [];
int counter = 0;

// This is the mock function that replaces the real function.
@test:Mock {
    moduleName: "ballerina/io",
    functionName: "println"
}
public function mockPrint(any... s) {
    string outStr;
    foreach str in s{
        outStr = outStr + <string>str;
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
    boolean assert = false;
    if((outputs[0] == "sum of first 10000000 positive numbers = 50000005000000") &&
                    (outputs[1] == "sum of squares of first 10000000 positive numbers = 1291990006563070912")) {
        assert = true;
    } else if ((outputs[1] == "sum of first 10000000 positive numbers = 50000005000000") &&
                   (outputs[0] == "sum of squares of first 10000000 positive numbers = 1291990006563070912")) {
        assert = true;
    }
    test:assertTrue(assert);
}
