package client;
import ballerina.net.grpc;
import ballerina.io;

int total = 0;

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

endpoint helloWorldClient ep { host:"localhost", port:9090};

function main (string[] args) {
    error err = ep -> lotsOfReplies("Sam", typeof ServerMessageListener);

    if (err != null) {
        io:println("Error occured while sending event " + err.message);
    }

    while (total == 0) {}
    io:println("Client got response successfully.");
}

service<grpc:Listener> ServerMessageListener {

    onMessage (string message) {
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

