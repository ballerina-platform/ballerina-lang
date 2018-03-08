package client;

import ballerina.log;
import ballerina.io;

int total = 0;
function main (string[] args) {

    endpoint<helloWorldNonBlockingStub> helloWorldStubNonBlocking {
        create helloWorldNonBlockingStub("localhost", 9090);
    }
    grpc:ClientConnection conn = {};
    error err = {};

    conn,err = helloWorldStubNonBlocking.LotsOfGreetings("ServerMessageListener");
    if (err != null) {
        io:println(err.message);
    } else {
        log:printInfo("Client initial connection sucessfully.");
    }

    grpc:ConnectorError errr = conn.send("Hi from WSO2");
    if (errr != null) {
        io:println(err.message);
    } else {
        log:printInfo("Client say : Hi from WSO2");
    }

    errr = conn.send("Hi from IBM");
    if (errr != null) {
        io:println(err.message);
    } else {
        log:printInfo("Client say : Hi from IBM");
    }

    errr = conn.send("Hi from Apache");
    if (errr != null) {
        io:println(err.message);
    } else {
        log:printInfo("Client say : Hi from Apache");
    }

    errr = conn.complete();
    if (errr != null) {
        io:println(err.message);
    } else {
        log:printInfo("Client say I'm done.");
    }

    while (total == 0) {}

    io:println("Client got all responses successfully.");

}

@grpc:messageListener {}
service<grpc> ServerMessageListener {

    resource onMessage (grpc:ClientConnection conn, string message) {
        io:println("Responce received from server: " + message);
        total = 1;
    }

    resource onError (grpc:ClientConnection conn, grpc:ServerError err) {
        if (err != null) {
            io:println("Error reported from server: " + err.message);
        }
    }

    resource onComplete (grpc:ClientConnection conn) {
        log:printInfo("Server Complete Sending Responces.");
    }
}
