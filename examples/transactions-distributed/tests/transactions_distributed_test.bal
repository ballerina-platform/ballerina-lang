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

    test:assertTrue(foundMatch(untaint outputs, "Sent response back to initiator"));
    test:assertTrue(foundMatch(untaint outputs, "Got response from bizservice"));

    test:assertTrue(foundMatch(untaint outputs,  "Initiated transaction committed"));
    test:assertTrue(foundMatch(untaint outputs, "Sent response back to client"));
}

function foundMatch(string[] arr, string target) returns boolean {
    foreach var out in outputs {
        if (out == target) {
            return true;
        }
    }
    return false;
}
