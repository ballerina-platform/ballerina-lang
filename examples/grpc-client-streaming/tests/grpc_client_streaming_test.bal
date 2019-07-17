// This is the B7a test for the client streaming scenario.
import ballerina/io;
import ballerina/runtime;
import ballerina/test;

// Client endpoint configuration.
HelloWorldClient helloWorldEp = new("http://localhost:9090");
boolean completed = false;
string responseMsg = "";

@test:Config
function testClientStreamingService() {
    grpc:StreamingClient ep;
    // Execute the unary non-blocking call that registers a server message listener.
    var res = helloWorldEp->lotsOfGreetings(MessageListener);
    if (res is error) {
        string errorMsg = "Error from Connector: " + res.reason() + " - " + <string> res.detail()["message"];
        test:assertFail(msg = errorMsg);
        return;
    } else {
        io:println("Initialized connection sucessfully.");
        ep = res;
    }

    // Send multiple messages to the server.
    string[] greets = ["Hi", "Hey", "GM"];
    var name = "John";
    foreach string greet in greets {
        error? connErr = ep->send(greet + " " + name);
        if (connErr is error) {
            string errorMsg = "Error from Connector: " + connErr.reason() + " - " + <string> connErr.detail()
            ["message"];
            test:assertFail(msg = errorMsg);
        } else {
            io:println("Send greeting: " + greet + " " + name);
        }
    }
    // Once all the messages are sent, the server notifies the caller with a `complete` message.
    checkpanic ep->complete();

    int waitCount = 0;
    while(!completed) {
        runtime:sleep(1000);
        if (waitCount > 10) {
            break;
        }
        waitCount += 1;
    }
    test:assertEquals(completed, true, msg = "Incomplete response message.");
    string expected = "Ack";
    test:assertEquals(responseMsg, expected);
}

// Server Message Listener.
service MessageListener = service {

    // Resource registered to receive server messages.
    resource function onMessage(string message) {
        completed = true;
        responseMsg = untaint message;
        io:println("Response received from server: " + message);
    }

    // Resource registered to receive server error messages.
    resource function onError(error err) {
        completed = true;
        responseMsg = "Error from Connector: " + <@untainted> err.reason() + " - " + <@untainted> <string> err.detail()["message"];
    }

    // Resource registered to receive server completed messages.
    resource function onComplete() {
        completed = true;
        io:println("Server Complete Sending Responses.");
    }
};
