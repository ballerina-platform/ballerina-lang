// This is the client implementation for the client streaming scenario.
import ballerina/io;

int total = 0;
function main(string... args) {
    // Client endpoint configuration.
    endpoint HelloWorldClient helloWorldEp {
        url: "http://localhost:9090"
    };

    endpoint grpc:Client ep;
    // Execute the unary non-blocking call that registers a server message listener.
    var res = helloWorldEp->lotsOfGreetings
    (HelloWorldMessageListener);

    match res {
        error err => {
            io:print("error");
        }
        grpc:Client con => {
            ep = con;
        }
    }

    io:print("Initialized connection sucessfully.");

    // Send multiple messages to the server.
    string[] greets = ["Hi", "Hey", "GM"];
    var name = "John";
    foreach greet in greets {
        error? connErr = ep->send(greet + " " + name);
        io:println(connErr.message but { () => "send greeting: " +
                greet + " " + name });
    }

    // Once all the messages are sent, the server notifies the caller with a `complete` message.
    _ = ep->complete();

    while (total == 0) {}
    io:println("completed successfully");
}

// Server Message Listener.
service<grpc:Service> HelloWorldMessageListener {

    // Resource registered to receive server messages.
    onMessage(string message) {
        total = 1;
        io:println("Response received from server: " + message);
    }

    // Resource registered to receive server error messages.
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

