// This is client implementation for bidirectional streaming scenario
package client;
import ballerina/net.grpc;
import ballerina/io;
import ballerina/log;

int total = 0;
function main (string[] args) {
    // Client endpoint configuration
    endpoint helloWorldClient helloWorldEp {
        host: "localhost",
        port: 9090
    };
    // Executing unary non-blocking call registering server message listener.
    var res = helloWorldEp -> LotsOfGreetings(typeof helloWorldMessageListener);
    grpc:ClientConnection ep ={};
    match res {
        grpc:error err => {
            io:print("error");
        }
        grpc:ClientConnection con => {
            ep = con;
        }
       }
    ChatMessage mes = {};
    mes.name = "Sam";
    mes.message = "Hi ";
    grpc:ConnectorError connErr = ep.send(mes);
    if (connErr != null) {
        io:println("Error at LotsOfGreetings : " + connErr.message);
    }
    mes.name = "Sam";
    mes.message = "How is the day?";
    connErr = ep.send(mes);
    if (connErr != null) {
    io:println("Error at LotsOfGreetings : " + connErr.message);
    }
    //this will hold forever since this is chat application
    while (total ==0) {}
    _ = ep.complete();
}

// Server Message Listener.
service<grpc:Listener> helloWorldMessageListener {

    // Resource registered to receive server messages
    onMessage (string message) {
        io:println("Responce received from server: " + message);
    }

    // Resource registered to receive server error messages
    onError (grpc:ServerError err) {
        if (err != null) {
            io:println("Error reported from server: " + err.message);
        }
    }

    // Resource registered to receive server completed message.
    onComplete () {
        io:println("Server Complete Sending Responses.");
    }
}

