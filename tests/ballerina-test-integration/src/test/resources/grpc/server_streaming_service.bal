// This is server implementation for server streaming scenario
import ballerina/io;
import ballerina/log;
import ballerina/grpc;

// Server endpoint configuration
endpoint grpc:Service ep {
    host:"localhost",
    port:9090
};

@grpc:serviceConfig {generateClientConnector:false}
service<grpc:Listener> HelloWorld bind ep {

    @grpc:resourceConfig {streaming:true}
    lotsOfReplies (endpoint client, string name) {
        log:printInfo("Server received hello from " + name);
        string[] greets = ["Hi", "Hey", "GM"];
        foreach greet in greets {
            log:printInfo("send reply: " + greet + " " + name);
            grpc:ConnectorError err = client -> send(greet + " " + name);
            if (err != ()) {
                io:println("Error at lotsOfReplies : " + err.message);
            }
        }
        // Once all messages are sent, server send complete message to notify the client, I’m done.
        _ = client -> complete();
        log:printInfo("send all responses sucessfully.");
    }

}
