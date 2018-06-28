// This is the server implementation for the unary blocking/unblocking scenario.
import ballerina/io;
import ballerina/grpc;

// The server endpoint configuration.
endpoint grpc:Listener listener {
    host: "localhost",
    port: 9090
};

service HelloWorld bind listener {

    hello(endpoint caller, string name, grpc:Headers headers) {
        io:println("name: " + name);
        string message = "Hello " + name;
        // Reads custom headers in request message.
        io:println(headers.get("Keep-Alive"));

        // Writes custom headers to response message.
        grpc:Headers resHeader = new;
        resHeader.setEntry("Host", "ballerina.io");

        // Sends response message with headers.
        error? err = caller->send(message, headers = resHeader);
        io:println(err.message but { () => "Server send response : " +
                message });

        // Sends `completed` notification to caller.
        _ = caller->complete();
    }
}
