import ballerina.io;
import ballerina.log;
import ballerina.net.grpc;

@grpc:serviceConfig {port:9090}
service<grpc> helloWorld {

    @grpc:resourceConfig {streaming:true}
    resource lotsOfReplies (grpc:ServerConnection conn, string name) {
        log:printInfo("Server received 'Hello' from " + name);
        string[] greets = ["WSO2", "IBM", "Apache"];
        foreach greet in greets {
            log:printInfo("Server send, hello from  " + greet + ".");
            grpc:ConnectorError err = conn.send("Hello from  " + greet + ".");
            if (err != null) {
                io:println("Error at lotsOfReplies : " + err.message);
            }
        }
        // Once all messages are sent, server send complete message to notify the client, I’m done.
        _ = conn.complete();
        log:printInfo("Server send all responses sucessfully.");
    }
}