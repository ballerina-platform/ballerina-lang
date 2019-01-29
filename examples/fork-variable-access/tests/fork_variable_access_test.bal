import ballerina/test;
import ballerina/io;

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
    test:assertEquals(outputs[0], "[value type variables] before fork: value of integer variable is [100] value of string variable is [WSO2]");
    test:assertEquals(outputs[1], "[reference type variables] before fork: value of name is [Bert] value of city is [New York] value of postcode is [10001]");
    test:assertEquals(outputs[2], "[value type variables] after fork: value of integer variable is [100] value of string variable is [WSO2]");
    test:assertEquals(outputs[3], "[reference type variables] after fork: value of name is [Moose] value of city is [Manhattan] value of street is [Wall Street] value of postcode is [10001]");
}
