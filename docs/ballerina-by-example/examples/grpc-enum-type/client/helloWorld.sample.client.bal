// This is client implementation for unary blocking scenario
package client;

import ballerina/io;

function main (string[] args) {
    // Client endpoint configuration
    endpoint helloWorldBlockingClient helloWorldBlockingEp {
        host:"localhost",
        port:9090
    };

    // Executing unary blocking call
    Request req = {};
    req.name = "IBM";
    req.kind = Foo.WSO2;
    Response|error unionResp = helloWorldBlockingEp -> hello(req);
    match unionResp {
        Response payload => {
            io:println("Client Got Response : ");
            io:println(payload);
        }
        error err => {
            io:println("Error from Connector: " + err.message);
        }
    }
}


