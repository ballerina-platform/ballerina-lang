// This is server implementation for client streaming scenario
import ballerina/io;
import ballerina/grpc;

// Server endpoint configuration
endpoint grpc:Listener ep {
    host:"localhost",
    port:9090
};

@grpc:serviceConfig {name:"lotsOfGreetings",
    clientStreaming:true}
service HelloWorld bind ep {
    onOpen (endpoint caller) {
        io:println("connected sucessfully.");
    }

    onMessage (endpoint caller, string name) {
        io:println("greet received: " + name);
    }

    onError (endpoint caller, error err) {
        if (err != ()) {
            io:println("Something unexpected happens at server : " + err.message);
        }
    }

    onComplete (endpoint caller) {
        io:println("Server Response");
        error? err = caller -> send("Ack");
        io:println(err.message but {() => ("Server send response : Ack")});
    }
}
