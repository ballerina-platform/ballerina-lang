// This is the server implementation for the unary blocking/unblocking scenario.
import ballerina/grpc;
import ballerina/log;

// Bind the `service` to the port.
service HelloWorld on new grpc:Listener(9090) {

    resource function hello(grpc:Caller caller, string name) {
        log:printInfo("Server received hello from " + name);
        string message = "Hello " + name;

        // Send a response message to the caller.
        error? result = caller->send(message);
        if (result is error) {
            log:printError("Error from Connector: " + result.reason() + " - "
                    + <string> result.detail()["message"]);
        }
        // Send the `completed` notification to the caller.
        result = caller->complete();
        if (result is error) {
            log:printError("Error from Connector: " + result.reason() + " - "
                    + <string> result.detail()["message"]);
        }
    }
}
