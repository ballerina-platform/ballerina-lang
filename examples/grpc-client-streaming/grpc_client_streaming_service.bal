// This is the server implementation for the client streaming scenario.
import ballerina/grpc;
import ballerina/log;

@grpc:ServiceConfig {
    name: "lotsOfGreetings",
    clientStreaming: true
}
service HelloWorld on new grpc:Listener(9090) {

    //This `resource` is triggered when a new caller connection is initialized.
    resource function onOpen(grpc:Caller caller) {
        log:printInfo("Client connected sucessfully.");
    }

    //This `resource` is triggered when the caller sends a request message to the service.
    resource function onMessage(grpc:Caller caller, string name) {
        log:printInfo("Server received greet: " + name);
    }

    //This `resource` is triggered when the server receives an error message from the caller.
    resource function onError(grpc:Caller caller, error err) {
        log:printError("Error from Connector: " + err.reason() + " - "
                                           + <string>err.detail()["message"]);
    }

    //This `resource` is triggered when the caller sends a notification to the server to indicate that it has finished sending messages.
    resource function onComplete(grpc:Caller caller) {
        grpc:Error? err = caller->send("Ack");
        if (err is grpc:Error) {
            log:printError("Error from Connector: " + err.reason() + " - "
                                           + <string>err.detail()["message"]);
        } else {
            log:printInfo("Server send response : Ack");
        }
    }
}
