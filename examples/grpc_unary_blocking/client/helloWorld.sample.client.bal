// This is client implementation for unary blocking scenario
import ballerina/io;
import ballerina/grpc;

function main(string... args) {
    // Client endpoint configuration
    endpoint HelloWorldBlockingClient helloWorldBlockingEp {
        url:"http://localhost:9090"
    };

    //Working with custom headers
    grpc:Headers headers = new;
    headers.setEntry("Keep-Alive", "300");

    // Executing unary blocking call
    var unionResp = helloWorldBlockingEp->hello("WSO2", headers);
    match unionResp {
        (string, grpc:Headers) payload => {
            string result;
            grpc:Headers resHeaders;
            (result, resHeaders) = payload;
            io:println("Client Got Response : ");
            io:println(payload);
            string? headerValue = resHeaders.get("Host");
            io:println("Headers: " + headerValue);
        }
        error err => {
            io:println("Error from Connector: " + err.message);
        }
    }

}
