// This is client implementation for unary non blocking scenario
import ballerina/io;

int total = 0;
function main(string... args) {
    // Client endpoint configuration
    endpoint HelloWorldClient helloWorldEp {
        url:"http://localhost:9090"
    };
    // Executing unary non-blocking call registering server message listener.
    error|() result = helloWorldEp->hello("WSO2", HelloWorldMessageListener);
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

    // Resource registered to receive server completed message.
    onComplete() {
        io:println("Server Complete Sending Response.");
        total = 1;
    }
}

