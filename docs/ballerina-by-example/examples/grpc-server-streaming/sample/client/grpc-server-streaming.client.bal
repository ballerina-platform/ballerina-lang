package sample.client;

import ballerina.io;

int total = 0;
function main (string[] args) {
    endpoint<helloWorldNonBlockingStub> helloWorldStubNonBlocking {
        create helloWorldNonBlockingStub("localhost", 9090);
    }

    _ = helloWorldStubNonBlocking.lotsOfReplies("Sri Lanka", "ServerMessageListener");

    while (total == 0) {}

    io:println("Client got responses successfully.");
}

@grpc:messageListener {}
service<grpc> ServerMessageListener {

    resource onMessage (grpc:ClientConnection conn, string message) {
        io:println("Responce received from server: " + message + " server.");
    }

    resource onError (grpc:ClientConnection conn, grpc:ServerError err) {
        if (err != null) {
            io:println("Error reported from server: " + err.message);
        }
    }

    resource onComplete (grpc:ClientConnection conn) {
        total = 1;
        io:println("Server Complete Sending Responces.");
    }
}

