// This is the client implementation for the server streaming scenario.
import ballerina/io;

int total = 0;
function main(string... args) {
    // Client endpoint configuration.
    endpoint HelloWorldClient helloWorldEp {
        url: "http://localhost:9090"
    };

    // Execute the unary non-blocking call that registers the server message listener.
    error? result = helloWorldEp->lotsOfReplies("Sam",
                                                    HelloWorldMessageListener);

    match result {
        error err => {
            io:println("Error occured while sending event " + err.message);
        }
        () => {
            io:println("Connected successfully");
        }
    }

    while (total == 0) {}
    io:println("Client got response successfully.");
}

// Server Message Listener.
service<grpc:Service> HelloWorldMessageListener {

    // Resource registered to receive server messages
    onMessage(string message) {
        io:println("Response received from server: " + message);
    }

    // Resource registered to receive server error messages
    onError(error err) {
        if (err != ()) {
            io:println("Error reported from server: " + err.message);
        }
    }

    // Resource registered to receive server completed messages.
    onComplete() {
        total = 1;
        io:println("Server Complete Sending Responses.");
    }
}

