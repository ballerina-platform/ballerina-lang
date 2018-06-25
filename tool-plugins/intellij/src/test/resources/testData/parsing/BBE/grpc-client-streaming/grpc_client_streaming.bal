// This is the server implementation for the client streaming scenario.
import ballerina/io;
import ballerina/grpc;

// The server endpoint configuration.
endpoint grpc:Listener listener {
    host: "localhost",
    port: 9090
};

@grpc:ServiceConfig {
    name: "lotsOfGreetings",
    clientStreaming: true
}
service<grpc:Service> HelloWorld bind listener {

    //This resource is triggered when a new caller connection is initialized.
    onOpen(endpoint caller) {
        io:println("connected sucessfully.");
    }

    //This resource is triggered when the caller sends a request message to the service.
    onMessage(endpoint caller, string name) {
        io:println("greet received: " + name);
    }

    //This resource is triggered when the server receives an error message from the caller.
    onError(endpoint caller, error err) {
        if (err != ()) {
            io:println("Something unexpected happens at server : "
                                                                + err.message);
        }
    }

    //This resource is triggered when the caller sends a notification to the server to indicate that it has finished sending messages.
    onComplete(endpoint caller) {
        io:println("Server Response");
        error? err = caller->send("Ack");
        io:println(err.message but { () => "Server send response : Ack" });
    }
}
