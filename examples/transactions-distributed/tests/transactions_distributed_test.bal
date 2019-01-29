import ballerina/test;
import ballerina/log;

string[] outputs = [];
int count = 0;

@test:Mock {
    moduleName: "ballerina/log",
    functionName: "printInfo"
}
public function mockLogInfo(string | (function() returns (string)) msg) {
    if (msg is string) {
        outputs[count] = msg;
        count += 1;
    }
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
    test:assertTrue(<boolean> outputs[1].matches("Created transaction: .*"));
    test:assertTrue(<boolean> outputs[2].matches("Started transaction: .*"));
    test:assertTrue(<boolean> outputs[3].matches("Registered local participant: .*"));
    test:assertEquals(outputs[4], "Received update stockquote request");
    test:assertTrue(<boolean> outputs[5].matches("Joined transaction: .*"));
    test:assertTrue(<boolean> outputs[6].matches("Update stock quote request received.\n.*"));
    test:assertEquals(outputs[7], "Sent response back to initiator");
    test:assertEquals(outputs[8], "Got response from bizservice");
    test:assertTrue(<boolean> outputs[9].matches("Running 2-phase commit for transaction: .*"));
    test:assertTrue(<boolean> outputs[10].matches("Preparing local participant: .*"));
    test:assertTrue(<boolean> outputs[11].matches("Local participant: .* prepared"));
    test:assertTrue(<boolean> outputs[12].matches("Participated transaction: .* committed"));
    test:assertEquals(outputs[13], "Initiated transaction committed");
    test:assertEquals(outputs[14], "Sent response back to client");
}
