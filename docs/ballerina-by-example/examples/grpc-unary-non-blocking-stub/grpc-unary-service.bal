import ballerina.io;
import ballerina.net.grpc;

@grpc:serviceConfig {port:9090,
                     generateClientConnector:false}
service<grpc> helloWorld {
    resource hello (grpc:ServerConnection conn, string name) {
        io:println("Received message from : " + name);
        string message = "Hello " + name; // response message
        grpc:ConnectorError err = conn.send(message);
        if (err != null) {
            io:println("Error at helloWorld : " + err.message);
        }
        _ = conn.complete();
    }
}
