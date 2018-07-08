// This is client implementation for unary blocking scenario.
import ballerina/io;
import ballerina/grpc;

function main(string... args) {
    // Client endpoint configuration
    endpoint HelloWorldBlockingClient helloWorldBlockingEp {
        url: "http://localhost:9090"
    };

    // Writes custom headers to request message.
    grpc:Headers headers = new;
    headers.setEntry("client_header_key", "Request Header Value");

    // Executes unary blocking call with headers.
    var unionResp = helloWorldBlockingEp->hello("WSO2", headers = headers);

    // Reads message and headers from response.
    match unionResp {
        (string, grpc:Headers) payload => {
            string result;
            grpc:Headers resHeaders;
            (result, resHeaders) = payload;
            io:println("Client Got Response : ");
            io:println(result);
            string headerValue = resHeaders.get("server_header_key") ?: "none";
            io:println("Headers: " + headerValue );
        }
        error err => {
            io:println("Error from Connector: " + err.message);
        }
    }

}
