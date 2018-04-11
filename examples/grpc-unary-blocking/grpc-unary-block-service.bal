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
        io:println("name: " + name);
        string message = "Hello " + name;
        // Working with custom headers.
        grpc:MessageContext context = client -> getContext();
        io:println(context.getHeader("x-id"));
        context.setHeader("x-id", "1234567890");
        grpc:ConnectorError err = client -> send(message);
        io:println("Server send response : " + message);
        if (err != ()) {
            io:println("Error at helloWorld : " + err.message);
        }
        _ = client -> complete();
    }
}
