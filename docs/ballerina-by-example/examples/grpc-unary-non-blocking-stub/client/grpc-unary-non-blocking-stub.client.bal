package client;
import ballerina.io;
import ballerina.log;

int total = 0;
function main (string[] args) {

    endpoint<helloWorldNonBlockingStub> helloWorldStubNonBlocking {
        create helloWorldNonBlockingStub("localhost", 9090);
    }

    error err = helloWorldStubNonBlocking.hello("Hi from WSO2", "ServerMessageListener");

    if (err != null) {
        io:println("Error occured while sending event " + err.message);
    }

    while (total == 0) {}

    io:println("Client got responses successfully.");
}


@grpc:messageListener {}
service<grpc> ServerMessageListener {

    resource onMessage (grpc:ClientConnection conn, string message) {
        io:println("Responce received from server: " + message);
    }

    resource onError (grpc:ClientConnection conn, grpc:ServerError err) {
        if (err != null) {
            io:println("Error repoted from server " + err.message);
        }
    }

    resource onComplete (grpc:ClientConnection conn) {
        log:printInfo("Server Complete Sending Responces.");
        total = 1;
    }
}