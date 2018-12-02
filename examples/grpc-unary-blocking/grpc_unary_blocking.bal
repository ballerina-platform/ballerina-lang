// This is the server implementation for the unary blocking/unblocking scenario.
import ballerina/grpc;
import ballerina/io;

<<<<<<< HEAD
// The server endpoint configuration.
listener grpc:Listener ep = new ({
    host:"localhost",
    port:9090
});

@grpc:ServiceDescriptor {
    descriptor: string.convert(descriptorMap[DESCRIPTOR_KEY]),
    descMap: descriptorMap
}
service HelloWorld on ep {
=======
service HelloWorld on new grpc:Listener(9090) {
>>>>>>> a27307a7097819dafae8767e97e36870f755ea10

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
