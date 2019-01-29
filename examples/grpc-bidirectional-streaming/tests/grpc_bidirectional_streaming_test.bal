// This is the B7a test for bidirectional streaming scenario.
import ballerina/io;
import ballerina/runtime;
import ballerina/test;

//Client endpoint configuration.
ChatClient chatEp = new("http://localhost:9090");
boolean received = false;
string responseMsg = "";

@test:Config
function testBidiStreamingService() {
    grpc:StreamingClient ep;
    // Executes unary non-blocking call registering server message listener.
    var res = chatEp->chat(MessageListener);
    if (res is error) {
        string errorMsg = "Error from Connector: " + res.reason() + " - " + <string>res.detail().message;
        test:assertFail(msg = errorMsg);
        return;
    } else {
        io:println("Initialized connection sucessfully.");
        ep = res;
    }

    // Sends multiple messages to the server.
    ChatMessage mes = { name: "Sam", message: "Hi" };
    error? connErr = ep->send(mes);
    if (connErr is error) {
        string errorMsg = "Error from Connector: " + connErr.reason() + " - " + <string>connErr.detail().message;
        test:assertFail(msg = errorMsg);
    }

    int waitCount = 0;
    while(!received) {
        io:println(responseMsg);
        runtime:sleep(1000);
        if (waitCount > 10) {
            break;
        }
        waitCount += 1;
    }
    test:assertEquals(received, true, msg = "Server message didn't receive.");
    string expected = "Sam: Hi";
    test:assertEquals(responseMsg, expected);
    // Once all messages are sent, client send complete message to notify the server, I am done.
    _ = ep->complete();
}

service MessageListener = service {

    // Resource registered to receive server messages.
    resource function onMessage(string message) {
        responseMsg = untaint message;
        received = true;
        io:println("Response received from server: " + message);
    }

    // Resource registered to receive server error messages.
    resource function onError(error err) {
        responseMsg = "Error reported from server: " + untaint err.reason() + " - "
                                                                            + untaint <string>err.detail().message;
        received = true;
    }

    // Resource registered to receive server completed message.
    resource function onComplete() {
        received = true;
        io:println("Server Complete Sending Responses.");
    }
};
