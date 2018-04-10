// This is the client implementation for the client streaming scenario.
import ballerina/io;
import ballerina/log;

int total = 0;
function main (string[] args) {
    // The client endpoint configuration.
    endpoint HelloWorldClient helloWorldEp {
        host:"localhost",
        port:9090
    };

    endpoint grpc:Client ep;
    // This executes the non-blocking call and registers the server message listener:
    var res = helloWorldEp -> lotsOfGreetings(typeof HelloWorldMessageListener);
    match res {
        grpc:error err => {
            io:print("error");
        }
        grpc:Client con => {
            ep = con;
        }
    }

    log:printInfo("Initialized connection sucessfully.");

    string[] greets = ["Hi", "Hey", "GM"];
    var name = "John";
    foreach greet in greets {
        log:printInfo("send greeting: " + greet + " " + name);
        grpc:ConnectorError connErr = ep -> send(greet + " " + name);
        if (connErr != ()) {
            io:println("Error at lotsOfGreetings : " + connErr.message);
        }
    }
    _ = ep -> complete();

    //This holds the program.
    while (total == 0) {}
    io:println("completed successfully");
}

// The server message listener.
service<grpc:Listener> HelloWorldMessageListener {

    // The resource registered to receive server messages.
    onMessage (string message) {
        total = 1;
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

