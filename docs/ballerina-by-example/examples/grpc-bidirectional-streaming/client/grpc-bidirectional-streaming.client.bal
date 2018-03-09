package client;

import ballerina.log;
import ballerina.io;

int complete = 0;
function main (string[] args) {
    
    endpoint<chatServerNonBlockingStub> chatServerStubNonBlocking {
        create chatServerNonBlockingStub("localhost", 9090);
    }

    grpc:ClientConnection conn = {};
    error err = {};

    conn,err = chatServerStubNonBlocking.chat("ServerMessageListener");
    if (err != null) {
        io:println(err.message);
    } else {
        log:printInfo("Client initial connection sucessfully.");
    }

    ChatMessage message = {};
    message.from = "Sam";
    message.message = "Hi all";

    grpc:ConnectorError errr = conn.send(message);
    if (errr != null) {
        io:println(err.message);
    } else {
        io:print("message : ");
        io:println(message);
    }
   
    while(complete ==0){}
}

@grpc:messageListener {}
service<grpc> ServerMessageListener {

    resource onMessage (grpc:ClientConnection conn, string message) {
        io:println("Responce received from server: " + message);

    }

    resource onError (grpc:ClientConnection conn, grpc:ServerError err) {
        if (err != null) {
            io:println("Error reported from server: " + err.message);
        }
    }

    resource onComplete (grpc:ClientConnection conn) {
        log:printInfo("Server Complete Sending Responces.");
        complete = 1;
    }
}
