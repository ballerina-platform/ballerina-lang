// This is client implementation for unary blocking scenario
import ballerina/io;

function main (string[] args) {
    // Client endpoint configuration
    endpoint HelloWorldBlockingClient helloWorldBlockingEp {
        host:"localhost",
        port:9090
    };

    // Executing unary blocking call
    string|error unionResp = helloWorldBlockingEp -> hello("WSO2");
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
