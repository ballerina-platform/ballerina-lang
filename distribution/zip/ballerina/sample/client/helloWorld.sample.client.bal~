package client;

import ballerina.io;
import ballerina.log;
int total = 0;

endpoint helloWorldClient ep { host:"localhost", port:9090};

public struct helloWorldClient {
    grpc:Client client;
    helloWorldStub stub;
}

public function <helloWorldClient ep> init(grpc:ClientEndpointConfiguration config) {
    // initialize client endpoint.
    grpc:Client client = {};
    client.init(config);
    ep.client = client;
    // initialize service stub.
    helloWorldStub stub = {};
    stub.initStub(client);
    ep.stub = stub;
}

public function <helloWorldClient ep> getClient() returns (helloWorldStub) {
    return ep.stub;
}

function main (string[] args) {
    var conn, err = ep -> LotsOfGreetings(typeof ServerMessageListener);
    if (err != null) {
        io:println(err);
	return;
    }
    grpc:Client ep1 = conn;
    log:printInfo("Initialized connection sucessfully.");

    string[] greets = ["Hi", "Hey", "GM"];
    var name = "John";
    foreach greet in greets {
        log:printInfo("send greeting: " + greet + " " + name);
        grpc:ConnectorError connErr = ep1.getClient().send(greet + " " + name);
        if (connErr != null) {
           io:println("Error at LotsOfGreetings : " + connErr.message);
        }
    }

    _ = ep1.getClient().complete();

    while (total == 0) {}

    io:println("completed successfully");
}

service<grpc:Listener> ServerMessageListener {

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
