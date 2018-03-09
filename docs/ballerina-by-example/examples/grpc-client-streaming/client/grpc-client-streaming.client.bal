package client;

import ballerina.io;
import ballerina.log;
int total = 0;
function main (string[] args) {

    endpoint<helloWorldNonBlockingStub> helloWorldStubNonBlocking {
        create helloWorldNonBlockingStub("localhost", 9090);
    }
    grpc:ClientConnection conn = {};
    error err = {};

    conn, err = helloWorldStubNonBlocking.LotsOfGreetings("ServerMessageListener");
    if (err != null) {
        io:println(err.message);
    } else {
        log:printInfo("Initialized connection sucessfully.");
    }

    string[] greets = ["Hi", "Hey", "GM"];
    var name = "John";
    foreach greet in greets {
        log:printInfo("send greeting: " + greet + " " + name);
        grpc:ConnectorError connErr = conn.send(greet + " " + name);
        if (connErr != null) {
           io:println("Error at LotsOfGreetings : " + connErr.message);
        }
    }

    _ = conn.complete();

    while (total == 0) {}

    io:println("completed successfully");
}

@grpc:messageListener {}
service<grpc> ServerMessageListener {

    resource onMessage (grpc:ClientConnection conn, string message) {
        io:println("Response received from server: " + message);
        total = 1;
    }

    resource onError (grpc:ClientConnection conn, grpc:ServerError err) {
        if (err != null) {
            io:println("Error reported from server: " + err.message);
        }
    }

    resource onComplete (grpc:ClientConnection conn) {
    }
}
