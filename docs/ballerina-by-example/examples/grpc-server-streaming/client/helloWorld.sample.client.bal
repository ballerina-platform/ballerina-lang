package client;
import ballerina.io;

int total = 0;
function main (string[] args) {

     endpoint helloWorldClient helloWorldEp {
            host: "0.0.0.0",
            port: 9090
        };
    error err = helloWorldEp -> lotsOfReplies("Sam", typeof helloWorldMessageListener);

    if (err != null) {
        io:println("Error occured while sending event " + err.message);
    }

    while (total == 0) {}
    io:println("Client got response successfully.");
}


service<grpc:Listener> helloWorldMessageListener {

    onMessage (string message) {
        io:println("Responce received from server: " + message);
    }

    onError (grpc:ServerError err) {
        if (err != null) {
            io:println("Error reported from server: " + err.message);
        }
    }

    onComplete () {
        total = 1;
        io:println("Server Complete Sending Responses.");
    }
}

