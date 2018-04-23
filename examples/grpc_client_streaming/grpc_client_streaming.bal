// This is server implementation for client streaming scenario
import ballerina/io;
import ballerina/grpc;

// Server endpoint configuration
endpoint grpc:Listener ep {
    host:"localhost",
    port:9090
};

@grpc:ServiceConfig {
    name:"lotsOfGreetings",
    clientStreaming:true
}
service<grpc:Service> HelloWorld bind ep {

    //This resource is triggered when a new service is initializing
    onOpen(endpoint caller) {
        io:println("connected sucessfully.");
    }

    //This resource is triggered when client need to send request to service
    onMessage(endpoint caller, string name) {
        io:println("greet received: " + name);
    }

    //This resource is triggered when some error has been happen at server end
    onError(endpoint caller, error err) {
        if (err != ()) {
            io:println("Something unexpected happens at server : " + err.message);
        }
    }

    //This resource is triggered when client notify service that it has finish send requests
    onComplete(endpoint caller) {
        io:println("Server Response");
        error? err = caller->send("Ack");
        io:println(err.message but { () => "Server send response : Ack" });
    }
}
