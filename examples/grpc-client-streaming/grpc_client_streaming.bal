// This is the server implementation for the client streaming scenario.
import ballerina/grpc;
import ballerina/io;

@grpc:ServiceConfig {
    name: "lotsOfGreetings",
    clientStreaming: true
}
service HelloWorld on new grpc:Listener(9090) {

    //This resource is triggered when a new caller connection is initialized.
    resource function onOpen(grpc:Caller caller) {
        io:println("connected sucessfully.");
    }

    //This resource is triggered when the caller sends a request message to the service.
    resource function onMessage(grpc:Caller caller, string name) {
        io:println("greet received: " + name);
    }

    //This resource is triggered when the server receives an error message from the caller.
    resource function onError(grpc:Caller caller, error err) {
        io:println("Error from Connector: " + err.reason() + " - "
                                            + <string>err.detail().message);
    }

    //This resource is triggered when the caller sends a notification to the server to indicate that it has finished sending messages.
    resource function onComplete(grpc:Caller caller) {
        io:println("Server Response");
        error? err = caller->send("Ack");
        if (err is error) {
            io:println("Error from Connector: " + err.reason() + " - "
                                               + <string>err.detail().message);
        } else {
            io:println("Server send response : Ack");
        }
    }
}
