// This is the server implementation for the secured connection (HTTPS) scenario.
import ballerina/grpc;
import ballerina/log;

// Server endpoint configuration with the SSL configurations.
listener grpc:Listener ep = new(9090, {
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
        log:printInfo("Server received hello from " + name);
        string message = "Hello " + name;

        // Send a response message to the caller.
        error? err = caller->send(message);

        if (err is error) {
            log:printError("Error from Connector: " + err.reason() + " - "
                                                    + <string> err.detail()["message"]);
        } else {
            log:printInfo("Server send response : " + message);
        }

        // Send the `completed` notification to the caller.
        error? result = caller->complete();
        if (result is error) {
            log:printError("Error in sending completed notification to caller",
                err = result);
        }
    }
}
