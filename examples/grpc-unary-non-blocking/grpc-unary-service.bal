// This is server implementation for unary blocking/unblocking scenario
import ballerina/io;
import ballerina/grpc;

// Server endpoint configuration
endpoint grpc:Service ep {
    host:"localhost",
    port:9090
};

@grpc:serviceConfig {generateClientConnector:false}
service<grpc:Listener> HelloWorld bind ep {
    hello (endpoint client, string name) {
        io:println("Received message from : " + name);
        string message = "Hello " + name; // response message
        grpc:ConnectorError err = client -> send(message);
        if (err != ()) {
            io:println("Error at helloWorld : " + err.message);
        }
        _ = client -> complete();
    }
}
