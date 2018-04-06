// This is server implementation for secured connection (HTTPS) scenario
import ballerina/io;

function main (string[] args) {
    endpoint HelloWorldBlockingClient helloWorldBlockingEp {
        host:"localhost",
        port:9090,
        ssl:{
            trustStoreFile:"${ballerina.home}/bre/security/ballerinaTruststore.p12",
            trustStorePassword:"ballerina"
        }
    };

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

