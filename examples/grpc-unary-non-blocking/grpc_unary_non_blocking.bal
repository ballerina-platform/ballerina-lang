// This is the server implementation for the unary blocking/unblocking scenario.
import ballerina/grpc;
import ballerina/io;

// Bind the `service` to the port.
service HelloWorld on new grpc:Listener(9090) {

    resource function hello(grpc:Caller caller, string name) {
        io:println("Received message from : " + name);
        string message = "Hello " + name;

        // Send a response message to the caller.
        error? err = caller->send(message);
        if (err is error) {
            io:println("Error from Connector: " + err.reason() + " - "
                                        + <string>err.detail().message);
        }
        // Send the `completed` notification to the caller.
        _ = caller->complete();
    }
}
