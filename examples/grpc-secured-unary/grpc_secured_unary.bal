// This is the server implementation for the secured connection (HTTPS) scenario.
import ballerina/grpc;
import ballerina/io;

// Server endpoint configuration with the SSL configurations.
listener grpc:Listener ep = new(9090, config = {
    host: "localhost",
    secureSocket: {
        keyStore: {
            path: "${ballerina.home}/bre/security/ballerinaKeystore.p12",
            password: "ballerina"
        }
    }
});

service HelloWorld on ep {
    resource function hello(grpc:Caller caller, string name) {
        io:println("name: " + name);
        string message = "Hello " + name;

        // Send a response message to the caller.
        error? err = caller->send(message);

        if (err is error) {
            io:println("Error from Connector: " + err.reason() + " - "
                                               + <string>err.detail().message);
        } else {
            io:println("Server send response : " + message);
        }

        // Send the `completed` notification to the caller.
        _ = caller->complete();

    }
}
