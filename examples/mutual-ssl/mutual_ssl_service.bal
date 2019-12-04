import ballerina/http;
import ballerina/log;

// Create an HTTP listener configuration, which will configure a listener to
// accept new connections that are secured via mutual SSL.
http:ListenerConfiguration helloWorldEPConfig = {
    secureSocket: {
        keyStore: {
            path: "${ballerina.home}/bre/security/ballerinaKeystore.p12",
            password: "ballerina"
        },
        trustStore: {
            path: "${ballerina.home}/bre/security/ballerinaTruststore.p12",
            password: "ballerina"
        },
         // Enable the preferred SSL protocol and its versions.
        protocol: {
            name: "TLS",
            versions: ["TLSv1.2", "TLSv1.1"]
        },
         // Configure the preferred ciphers.
        ciphers: ["TLS_ECDHE_RSA_WITH_AES_128_CBC_SHA"],
         // Enable mutual SSL.
        sslVerifyClient: "require"
    }
};

// Create a listener endpoint.
listener http:Listener helloWorldEP = new(9095, helloWorldEPConfig);

@http:ServiceConfig {
    basePath: "/hello"
}
// Bind the service to the listener endpoint that you declared earlier.
service helloWorld on helloWorldEP {

    @http:ResourceConfig {
        methods: ["GET"],
        path: "/"
    }
    resource function sayHello(http:Caller caller, http:Request req) {
        // Send the response to the caller.
        var result = caller->respond("Successful");
        if (result is error) {
            log:printError("Error in responding", err = result);
        }
    }
}
