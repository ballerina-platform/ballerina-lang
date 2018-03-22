package client;
import ballerina/net.grpc;
import ballerina/io;
import ballerina/log;

int total = 0;
function main (string[] args) {

    endpoint helloWorldClient helloWorldEp {
        host: "localhost",
        port: 9090
    };

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
        io:println("Server Complete Sending Responses.");
    }
}

