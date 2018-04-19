// This is server implementation for secured connection (HTTPS) scenario
import ballerina/io;
import ballerina/log;
import ballerina/grpc;

endpoint grpc:Listener ep {
    host:"localhost",
    port:9090,
    ssl:{
        keyStoreFile:"${ballerina.home}/bre/security/ballerinaKeystore.p12",
        keyStorePassword:"ballerina",
        certPassword:"ballerina"
    }

};
@grpc:serviceConfig
service<grpc:Service> HelloWorld bind ep {
    hello(endpoint caller, string name) {
        io:println("name: " + name);
        string message = "Hello " + name;
        error? err = caller->send(message);
        io:println(err.message but { () => "Server send response : " + message });
        _ = caller->complete();
    }
}
