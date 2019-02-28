// This is the B7a test for the server streaming scenario.
import ballerina/io;
import ballerina/runtime;
import ballerina/test;

// Client endpoint configuration.
HelloWorldClient streamingEp = new("http://localhost:9090");
boolean completed = false;
string respError = "";
string[] responseMsgs = [];
int msgCount = 0;

@test:Config
function testServerStreamingService() {
    // Execute the unary non-blocking call that registers the server message listener.
    error? result = streamingEp->lotsOfReplies("Sam", messageListener);
    if (result is error) {
        test:assertFail(msg = "Error from Connector: " + result.reason() + " - " + <string>result.detail().message);
    } else {
        io:println("Connected successfully");
    }

    int waitCount = 0;
    while(!completed && (responseMsgs.length() < 3)) {
        io:println(responseMsgs);
        runtime:sleep(1000);
        if (waitCount > 10) {
            break;
        }
        waitCount += 1;
    }
    test:assertEquals(completed, true, msg = "Incomplete response message.");
    string expectedMsg1 = "Hi Sam";
    string expectedMsg2 = "Hey Sam";
    string expectedMsg3 = "GM Sam";
    foreach string msg in responseMsgs {
        test:assertTrue(msg == expectedMsg1 || msg == expectedMsg2 || msg == expectedMsg3);
    }
    test:assertEquals(respError, "");
}

// Server Message Listener.
service messageListener = service {

    // Resource registered to receive server messages.
    resource function onMessage(string message) {
        responseMsgs[msgCount] = untaint message;
        msgCount = msgCount + 1;
    }

    // Resource registered to receive server error messages.
    resource function onError(error err) {
        respError = "Error from Connector: " + untaint err.reason() + " - " + untaint <string>err.detail().message;
    }

    // Resource registered to receive server completed messages.
    resource function onComplete() {
        test:assertTrue(true);
        io:println("Server Complete Sending Response.");
        completed = true;
    }
};
