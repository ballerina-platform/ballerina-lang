// This is server implementation for unary blocking/unblocking scenario
import ballerina/io;
import ballerina/grpc;

// Server endpoint configuration
endpoint grpc:Listener ep {
    host: "localhost",
    port: 9090
};

service HelloWorld bind ep {

    hello(endpoint caller, string name) {
        io:println("Received message from : " + name);
        string message = "Hello " + name; // response message
        error? err = caller->send(message);
        io:println(err.message but { () => "Server send response : " + message });
        _ = caller->complete();
    }
}
