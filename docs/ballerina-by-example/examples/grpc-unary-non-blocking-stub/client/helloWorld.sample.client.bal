package client;
import ballerina.io;

int total = 0;
function main (string[] args) {

     endpoint helloWorldClient helloWorldEp {
            host: "0.0.0.0",
            port: 9090
        };

    error err = helloWorldEp -> hello("WSO2", typeof helloWorldMessageListener);

    if (err != null) {
        io:println("Error occured while sending event " + err.message);
    }

    while (total == 0) {}
    io:println("Client got response successfully.");
}


service<grpc:Listener> helloWorldMessageListener {

    onMessage (string message) {
        io:println("Response received from server: " + message);
    }

    onError (grpc:ServerError err) {
        if (err != null) {
            io:println("Error reported from server: " + err.message);
        }
    }

    onComplete () {
        io:println("Server Complete Sending Response.");
	total = 1;
    }
}

