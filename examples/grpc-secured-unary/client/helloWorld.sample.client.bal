// This is server implementation for secured connection (HTTPS) scenario
import ballerina/io;

function main(string... args) {
    endpoint HelloWorldBlockingClient helloWorldBlockingEp {
        host:"localhost",
        port:9090,
        ssl:{
            trustStoreFile:"${ballerina.home}/bre/security/ballerinaTruststore.p12",
            trustStorePassword:"ballerina"
        }
    };

    var unionResp = helloWorldBlockingEp->hello("WSO2");
    match unionResp {
        (string, grpc:Headers) payload => {
            string result;
            (result, _) = payload;
            io:println("Client Got Response : ");
            io:println(result);
        }
        error err => {
            io:println("Error from Connector: " + err.message);
        }
    }
}

