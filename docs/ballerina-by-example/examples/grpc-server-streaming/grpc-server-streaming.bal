// This is the server implementation for the server streaming scenario.
import ballerina/io;
import ballerina/log;
import ballerina/grpc;

// The server endpoint configuration.
endpoint grpc:Service ep {
    host:"localhost",
    port:9090
};

@grpc:serviceConfig {generateClientConnector:false}
service<grpc:Endpoint> HelloWorld bind ep {

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
        // Once all the messages are sent, the server sends the message specified here to notify the client that its task is completed.
        _ = client -> complete();
        log:printInfo("send all responses sucessfully.");
    }

}
