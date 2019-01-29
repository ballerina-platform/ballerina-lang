import ballerina/test;
import ballerina/io;

string[] outputs = [];
int counter = 0;

// This is the mock function which will replace the real function
@test:Mock {
    moduleName: "ballerina/io",
    functionName: "println"
}
public function mockPrint(any... s) {
    outputs[counter] = string.convert(s[0]);
    counter += 1;
}

@test:Config
function testFunc() {
    // Invoking the main function
    main();
    xml xml1 = xml `<ns0:book xmlns:ns0="http://ballerina.com/aa" ns0:status="available" count="5"></ns0:book>`;
    string op1 = "available";
    string op2 = "available";
    string op3 = "5";
    string op4 = "5";
    string op5 = "Not Available";
    string op6 = "{\"{http://www.w3.org/2000/xmlns/}ns0\":\"http://ballerina.com/aa\", \"{http://ballerina.com/aa}status\":\"Not Available\", \"count\":\"5\"}";
    test:assertEquals(outputs[0], string.convert(xml1));
    test:assertEquals(outputs[1], op1);
    test:assertEquals(outputs[2], op2);
    test:assertEquals(outputs[3], op3);
    test:assertEquals(outputs[4], op4);
    test:assertEquals(outputs[5], op5);
    test:assertEquals(outputs[6], op6);
}
