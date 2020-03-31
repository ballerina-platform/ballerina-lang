// This is the client implementation for the unary non blocking scenario.
import ballerina/grpc;
import ballerina/io;

int total = 0;
public function main() {
    // Client endpoint configuration.
    HelloWorldClient helloWorldEp = new ("http://localhost:9090");

    // Execute the unary non-blocking call that registers the server message listener.
    grpc:Error? result = helloWorldEp->hello("WSO2", HelloWorldMessageListener);

    if (result is grpc:Error) {
        io:println("Error from Connector: " + result.reason() + " - "
                + <string>result.detail()["message"]);
    } else {
        io:println("Connected successfully");
    }

    while (total == 0) {}
    io:println("Client got response successfully.");
}

// Server Message Listener.
service HelloWorldMessageListener = service {

    // The `resource` registered to receive server messages.
    resource function onMessage(string message) {
        io:println("Response received from server: " + message);
    }

    // The `resource` registered to receive server error messages.
    resource function onError(error err) {
        io:println("Error reported from server: " + err.reason() + " - "
                + <string>err.detail()["message"]);
    }

    // The `resource` registered to receive server completed messages.
    resource function onComplete() {
        io:println("Server Complete Sending Response.");
        total = 1;
    }
};
