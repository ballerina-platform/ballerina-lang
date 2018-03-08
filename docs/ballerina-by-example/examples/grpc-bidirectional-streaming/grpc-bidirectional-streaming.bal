import ballerina.io;
import ballerina.net.grpc;

@grpc:serviceConfig {rpcEndpoint:"LotsOfGreetings",
                     clientStreaming:true,
                     serverStreaming:true,
                     generateClientConnector:false}
service<grpc> helloWorld {
    resource onOpen (grpc:ServerConnection conn) {
        io:println("Connnection has established sucessfully.");
    }

    resource onMessage (grpc:ServerConnection conn, string name) {
        string[] greets = ["WSO2", "IBM", "Apache"];
        foreach greet in greets {
            grpc:ConnectorError err = conn.send(greet + " " + name);
            io:println("Sending replies: " + name);
            if (err != null) {
                io:println("Error occured at sending replies : " + err.message);
            }
        }
        // Once all messages are sent, server send complete message to notify the client, I’m done.
        _ = conn.complete();
    }

    resource onError (grpc:ServerConnection conn, grpc:ServerError err) {
        if (err != null) {
            io:println("Something unexpected happens at server : " + err.message);
        }
    }

    resource onComplete (grpc:ServerConnection conn) {
        io:println("Client has completed Sending Requests.");
    }
}
