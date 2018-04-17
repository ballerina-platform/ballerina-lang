package client;

import ballerina/io;

function main (string... args) {

     endpoint helloWorldClient helloWorldEp {
        host: "localhost",
        port: 9090
     };


    endpoint helloWorldBlockingClient helloWorldBlockingEp {
        host: "localhost",
        port: 9090
    };

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
        io:println("Server Complete Sending Responses.");
    }
}

