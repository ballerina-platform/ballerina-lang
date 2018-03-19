package client;
import ballerina.io;

function main (string[] args) {
    endpoint helloWorldBlockingClient helloWorldBlockingEp {
            host: "0.0.0.0",
            port: 9090
        };

    var res, err = helloWorldBlockingEp -> hello("WSO2");
    if (err != null) {
        io:println("Error from Connector: " + err.message);
    } else {
        io:println("Client Got Responce : ");
        io:println(res);
    }
}


