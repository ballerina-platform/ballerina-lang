package client;
import ballerina/io;

function main (string[] args) {
    endpoint helloWorldBlockingClient helloWorldBlockingEp {
            host: "localhost",
            port: 9090
        };

    var res, err = helloWorldBlockingEp -> hello("WSO2");
    if (err != null) {
        io:println("Error from Connector: " + err.message);
    } else {
        io:println("Client Got Response : ");
        io:println(res);
    }
}


