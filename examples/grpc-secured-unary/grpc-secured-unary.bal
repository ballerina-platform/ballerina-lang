// This is server implementation for secured connection (HTTPS) scenario
import ballerina/io;
import ballerina/log;
import ballerina/grpc;

endpoint grpc:Service ep {
    host:"localhost",
    port:9090,
    ssl:{
        keyStoreFile:"${ballerina.home}/bre/security/ballerinaKeystore.p12",
        keyStorePassword:"ballerina",
        certPassword:"ballerina"
    }

};
@grpc:serviceConfig {generateClientConnector: false}
service<grpc:Listener> HelloWorld bind ep {
    hello (endpoint client, string name) {
        io:println("name: " + name);
        string message = "Hello " + name;
        grpc:ConnectorError err = client -> send(message);
        io:println("Server send response : " + message);
        if (err != ()) {
            io:println("Error at helloWorld : " + err.message);
        }
        _ = client -> complete();
    }
}
