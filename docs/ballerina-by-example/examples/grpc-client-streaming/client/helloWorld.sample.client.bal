package client;
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

    log:printInfo("Initialized connection sucessfully.");

    string[] greets = ["Hi", "Hey", "GM"];
    var name = "John";
    foreach greet in greets {
        log:printInfo("send greeting: " + greet + " " + name);
        grpc:ConnectorError connErr = ep.send(greet + " " + name);
        if (connErr != null) {
            io:println("Error at LotsOfGreetings : " + connErr.message);
        }
    }
    _ = ep.complete();

    //to hold the programme
    while (total == 0) {}
    io:println("completed successfully");
}


service<grpc:Listener> helloWorldMessageListener {

    onMessage (string message) {
        total = 1;
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

