// This is the client implementation for the client streaming scenario.
import ballerina/grpc;
import ballerina/io;

int total = 0;
public function main() {
    // Client endpoint configuration.
    HelloWorldClient helloWorldEp = new ("http://localhost:9090");

    grpc:StreamingClient ep;
    // Execute the unary non-blocking call that registers a server message listener.
    var res = helloWorldEp->lotsOfGreetings(HelloWorldMessageListener);

    if (res is grpc:Error) {
        io:println("Error from Connector: " + res.reason() + " - "
                                           + <string>res.detail()["message"]);
        return;
    } else {
        io:println("Initialized connection sucessfully.");
        ep = res;
    }

    // Send multiple messages to the server.
    string[] greets = ["Hi", "Hey", "GM"];
    var name = "John";
    foreach string greet in greets {
        grpc:Error? connErr = ep->send(greet + " " + name);
        if (connErr is grpc:Error) {
            io:println("Error from Connector: " + connErr.reason() + " - "
                                       + <string>connErr.detail()["message"]);
        } else {
            io:println("send greeting: " + greet + " " + name);
        }
    }

    // Once all the messages are sent, the server notifies the caller with a `complete` message.
    grpc:Error? result = ep->complete();
    if (result is grpc:Error) {
        io:println("Error in sending complete message", result);
    }

    while (total == 0) {}
    io:println("completed successfully");
}

// Server Message Listener.
service HelloWorldMessageListener = service {

    // Resource registered to receive server messages.
    resource function onMessage(string message) {
        total = 1;
        io:println("Response received from server: " + message);
    }

    // Resource registered to receive server error messages.
    resource function onError(error err) {
        io:println("Error reported from server: " + err.reason() + " - "
                                           + <string>err.detail()["message"]);
    }

    // Resource registered to receive server completed messages.
    resource function onComplete() {
        total = 1;
        io:println("Server Complete Sending Responses.");
    }
};
