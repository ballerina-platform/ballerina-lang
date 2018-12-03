// This is the server implementation for the unary blocking/unblocking scenario.
import ballerina/grpc;
import ballerina/io;

service HelloWorld on new grpc:Listener(9090) {

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
        if (err is error) {
            io:println("Error from Connector: " + err.reason() + " - "
                                                + <string>err.detail().message);
        }

        // Sends `completed` notification to caller.
        _ = caller->complete();
    }
}
