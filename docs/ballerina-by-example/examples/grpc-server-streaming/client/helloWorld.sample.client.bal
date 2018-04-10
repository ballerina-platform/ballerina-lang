// This is a client implementation for the server streaming scenario.
import ballerina/io;

int total = 0;
function main (string[] args) {
    // The client endpoint configuration.
    endpoint HelloWorldClient helloWorldEp {
        host:"localhost",
        port:9090
    };
    // This executes the unary non-blocking call and registers the server message listener.
    error? result = helloWorldEp -> lotsOfReplies("Sam", typeof HelloWorldMessageListener);
    match result {
        error payloadError => {
            io:println("Error occured while sending event " + payloadError.message);
        }
        () => {
            io:println("Connected successfully");
        }
    }

    while (total == 0) {}
    io:println("Client got response successfully.");
}

// The server message listener.
service<grpc:Listener> HelloWorldMessageListener {

    // The resource registered to receive server messages.
    onMessage (string message) {
        io:println("Response received from server: " + message);
    }

    // The resource registered to receive server error messages.
    onError (grpc:ServerError err) {
        if (err != ()) {
            io:println("Error reported from server: " + err.message);
        }
    }

    // The resource registered to receive the server completed message.
    onComplete () {
        total = 1;
        io:println("Server Complete Sending Responses.");
    }
}

