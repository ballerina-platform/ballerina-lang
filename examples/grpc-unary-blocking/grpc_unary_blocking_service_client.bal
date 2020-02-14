// This is client implementation for unary blocking scenario.
import ballerina/grpc;
import ballerina/io;

public function main() {
    // Client endpoint configuration.
    HelloWorldBlockingClient helloWorldBlockingEp = new ("http://localhost:9090");

    // Writes custom headers to request message.
    grpc:Headers headers = new;
    headers.setEntry("client_header_key", "Request Header Value");

    // Executes unary blocking call with headers.
    var unionResp = helloWorldBlockingEp->hello("WSO2", headers);

    // Reads message and headers from response.
    if (unionResp is grpc:Error) {
        io:println("Error from Connector: " + unionResp.reason() + " - "
                + <string>unionResp.detail()["message"]);
    } else {
        string result;
        grpc:Headers resHeaders;
        [result, resHeaders] = unionResp;
        io:println("Client Got Response : " + result);
        string headerValue = resHeaders.get("server_header_key") ?: "none";
        io:println("Headers: " + headerValue);
    }
}
