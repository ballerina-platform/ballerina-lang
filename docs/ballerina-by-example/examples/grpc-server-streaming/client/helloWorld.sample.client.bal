package client;
import ballerina/io;

int total = 0;
function main (string[] args) {

     endpoint helloWorldClient helloWorldEp {
            host: "localhost",
            port: 9090
        };
     var result = helloWorldEp -> lotsOfReplies("Sam", typeof helloWorldMessageListener);
     match result {
         error payloadError => {
            io:println("Error occured while sending event " + payloadError.message);
        }
        any | null => {
            io:println("Connected successfully");
          }
     }

    //to hold the programme
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

