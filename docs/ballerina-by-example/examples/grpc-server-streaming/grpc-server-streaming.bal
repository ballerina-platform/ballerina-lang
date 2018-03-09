import ballerina.io;
import ballerina.log;
import ballerina.net.grpc;

@grpc:serviceConfig {port:9090,
                     generateClientConnector:false}
service<grpc> helloWorld {

    @grpc:resourceConfig {streaming:true}
    resource lotsOfReplies (grpc:ServerConnection conn, string name) {
        log:printInfo("Server received hello from " + name);
        string[] greets = ["Hi", "Hey", "GM"];
        foreach greet in greets {
            log:printInfo("send reply: " + greet + " " + name);
            grpc:ConnectorError err = conn.send(greet + " " + name);
            if (err != null) {
                io:println("Error at lotsOfReplies : " + err.message);
            }
        }
        // Once all messages are sent, server send complete message to notify the client, I’m done.
        _ = conn.complete();
        log:printInfo("send all responses sucessfully.");
    }
}
