import ballerina/test;

string[] outputs = [];
int counter = 0;

// This is the mock function which will replace the real function
@test:Mock {
    moduleName: "ballerina/io",
    functionName: "println"
}
public function mockPrint(any... s) {
    if (s[0] is ()) {
        // Cannot convert () to string.
        outputs[counter] = "()";
    } else {
        outputs[counter] = s[0].toString();
    }
    counter += 1;
}

@test:Config
function testFunc() {
    // Invoking the main function
    main();
    string op0 = "<ns0:book xmlns:ns0=\"http://ballerina.com/aa\" ns0:status=\"available\" count=\"5\"/>";
    string op1 = "available";
    string op2 = "5";
    string op3 = "true";
    string op4 = "{http://www.w3.org/2000/xmlns/}ns0=http://ballerina.com/aa {http://ballerina.com/aa}status=available count=5";
    string op5 = "Not Available";
    test:assertEquals(outputs[0], op0);
    test:assertEquals(outputs[1], op1);
    test:assertEquals(outputs[2], op2);
    test:assertEquals(outputs[3], op3);
    test:assertEquals(outputs[4], op4);
    test:assertEquals(outputs[5], op1);
    test:assertEquals(outputs[6], op5);
}
