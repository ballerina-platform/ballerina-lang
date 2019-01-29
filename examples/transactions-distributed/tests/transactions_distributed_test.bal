import ballerina/test;
import ballerina/io;

any[] outputs = [];
int count = 0;

@test:Mock {
    moduleName: "ballerina/io",
    functionName: "println"
}
public function mockPrint(any... s) {
    outputs[count] = string.convert(s[0]);
    count += 1;
}

function initiateTrx() {
    http:Client httpEndpoint = new("http://localhost:8080");
    // Start the transaction by sending a get to "/"
    var response = httpEndpoint->get("/");
    if (response is http:Response) {
        test:assertTrue(true);
    } else {
        test:assertFail(msg = "Failed to call the initiator endpoint");
    }
}

@test:Config
function testFunc() {
    initiateTrx();

    test:assertEquals(outputs[0], "Initiating transaction...");

    string out1 = <string> outputs[1];
    test:assertTrue(out1.hasPrefix("Started transaction:"));

    test:assertEquals(outputs[2], "Received update stockquote request");

    string out3 = <string> outputs[3];
    test:assertTrue(out3.hasPrefix("Joined transaction: "));

    string out4 = <string> outputs[4];
    test:assertTrue(out4.hasPrefix("Update stock quote request received."));

    test:assertEquals(outputs[5], "Sent response back to initiator");
    test:assertEquals(outputs[6], "Got response from bizservice");

    string out7 = <string> outputs[7];
    test:assertTrue(out7.hasPrefix("Participated transaction:"));
    test:assertTrue(out7.hasSuffix("committed"));

    test:assertEquals(outputs[8], "Initiated transaction committed");
    test:assertEquals(outputs[9], "Sent response back to client");
}
