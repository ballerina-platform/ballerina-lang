package sample.client;

import ballerina.io;

function main (string[] args) {

    endpoint<helloWorldBlockingStub> helloWorldStubBlocking {
        create helloWorldBlockingStub("localhost", 9090);
    }

    HelloRequest req = {};
    req.name = "WSO2";
    var res,ee = helloWorldStubBlocking.hello(req);
    if (ee != null) {
        io:println("Error from Connector: " + ee.message);
    } else {
        io:println("Client Got Response : ");
        io:println(res);
    }
}