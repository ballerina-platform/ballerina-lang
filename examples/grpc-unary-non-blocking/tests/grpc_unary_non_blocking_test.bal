// This is the B7a test for the unary non blocking scenario.
import ballerina/io;
import ballerina/runtime;
import ballerina/test;

// Client endpoint configuration.
HelloWorldClient helloWorldEp = new("http://localhost:9090");
boolean completed = false;
string responseMsg = "";

@test:Config
function testUnaryNonBlockingClient() {
    // Execute the unary non-blocking call that registers the server message listener.
    error? result = helloWorldEp->hello("WSO2", messageListener);
    if (result is error) {
        string errorMsg = "Error from Connector: " + result.reason() + " - " + <string>result.detail().message;
        test:assertFail(msg = errorMsg);
    } else {
        io:println("Connected successfully");
    }

    int waitCount = 0;
    while(!completed) {
        runtime:sleep(1000);
        if (waitCount > 10) {
            break;
        }
        waitCount += 1;
    }
    test:assertEquals(completed, true, msg = "Incomplete response message.");
    string expected = "Hello WSO2";
    test:assertEquals(responseMsg, expected);
}

// Server Message Listener.
service messageListener = service {

    // Resource registered to receive server messages.
    resource function onMessage(string message) {
        responseMsg = untaint message;
    }

    // Resource registered to receive server error messages.
    resource function onError(error err) {
        responseMsg = "Error from Connector: " + untaint err.reason() + " - " + untaint <string>err.detail().message;
    }

    // Resource registered to receive server completed messages.
    resource function onComplete() {
        test:assertTrue(true);
        io:println("Server Complete Sending Response.");
        completed = true;
    }
};
