import ballerina/http;
import ballerina/log;

// `helloWorldEP` listener endpoint is configured to communicate through HTTPS.
// It is configured to listen on port 9095. As this is an HTTPS Listener,
// it is required to give the PKCS12 keystore file location and its password.
http:ListenerConfiguration helloWorldEPConfig = {
    secureSocket: {
        keyStore: {
            path: "${ballerina.home}/bre/security/ballerinaKeystore.p12",
            password: "ballerina"
        }
    }
};

listener http:Listener helloWorldEP = new (9095, config = helloWorldEPConfig);

@http:ServiceConfig {
    basePath: "/hello"
}
service helloWorld on helloWorldEP {

    @http:ResourceConfig {
        methods: ["GET"],
        path: "/"
    }
    resource function sayHello(http:Caller caller, http:Request req) {
        // Send the response back to the `caller`.
        var result = caller->respond("Hello World!");
        if (result is error) {
            log:printError("Failed to respond", err = result);
        }
    }
}
