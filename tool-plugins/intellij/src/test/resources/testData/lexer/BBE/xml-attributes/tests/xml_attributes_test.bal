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
    xml xml1 = xml `<ns0:book xmlns:ns0="http://ballerina.com/aa" ns0:status="available" count="5"></ns0:book>`;
    string op1 = "available";
    string op2 = "available";
    string op3 = "5";
    string op4 = "5";
    string op5 = "Not Available";
    string op6 = "{\"{http://www.w3.org/2000/xmlns/}ns0\":\"http://ballerina.com/aa\", \"{http://ballerina.com/aa}status\":\"Not Available\", \"count\":\"5\"}";
    test:assertEquals(xml1, outputs[0]);
    test:assertEquals(op1, outputs[1]);
    test:assertEquals(op2, outputs[2]);
    test:assertEquals(op3, outputs[3]);
    test:assertEquals(op4, outputs[4]);
    test:assertEquals(op5, outputs[5]);
    test:assertEquals(op6, <string>outputs[6]);
}
