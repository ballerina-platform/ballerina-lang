import ballerina.io;
import ballerina.net.grpc;

@grpc:serviceConfig {port:9090,
                     generateClientConnector:false}
service<grpc> helloWorld {
    resource hello (grpc:ServerConnection conn, string name) {
        io:println("Received hello from : " + name);
        string message = "Hello " + name;
        grpc:ConnectorError err = conn.send(message);
        io:println("Server send responce : " + message );
        if (err != null) {
            io:println("Error at helloWorld : " + err.message);
        }
        _ = conn.complete();
    }
}