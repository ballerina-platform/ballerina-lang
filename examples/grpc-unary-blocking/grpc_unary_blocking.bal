// This is the server implementation for the unary blocking/unblocking scenario.
import ballerina/grpc;
import ballerina/io;

// The server endpoint configuration.
listener grpc:Server server = new ({
    host:"localhost",
    port:9090
});

service HelloWorld on server {

    resource function hello (grpc:Caller caller, string name, grpc:Headers headers) {
        io:println("name: " + name);
        string message = "Hello " + name;
        // Reads custom headers in request message.
        io:println(headers.get("client_header_key"));

        // Writes custom headers to response message.
        grpc:Headers resHeader = new;
        resHeader.setEntry("server_header_key", "Response Header value");

        // Sends response message with headers.
        error? err = caller->send(message, headers = resHeader);
        io:println(err.message ?: "Server send response : " + message);

        // Sends `completed` notification to caller.
        _ = caller->complete();
    }
}
