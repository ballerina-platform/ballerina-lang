import ballerina.io;
import ballerina.net.grpc;

@grpc:serviceConfig {rpcEndpoint:"LotsOfGreetings",
                     clientStreaming:true}
service<grpc> helloWorld {
    resource onOpen (grpc:ServerConnection conn) {
        io:println("Connnection has established sucessfully.");
    }

    resource onMessage (grpc:ServerConnection conn, string name) {
        io:println("Server received request: " + name);
    }

    resource onError (grpc:ServerConnection conn, grpc:ServerError err) {
        if (err != null) {
            io:println("Something unexpected happens at server : " + err.message);
        }
    }

    resource onComplete (grpc:ServerConnection conn) {
        io:println("Server Response");
        grpc:ConnectorError err = conn.send("complete");
        if (err != null) {
            io:println("Error at onComplete send message : " + err.message);
        }
    }
}
