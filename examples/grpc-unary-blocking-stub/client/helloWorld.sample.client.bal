package client;
import ballerina/io;

function main (string[] args) {
    endpoint helloWorldBlockingClient helloWorldBlockingEp {
            host: "localhost",
            port: 9090
        };

    string | error unionResp = helloWorldBlockingEp -> hello("WSO2");
    match unionResp {
        string payload => {
            io:println("Client Got Response : ");
            io:println(payload);
        }
        error err => {
            io:println("Error from Connector: " + err.message);
        }
    }
}


