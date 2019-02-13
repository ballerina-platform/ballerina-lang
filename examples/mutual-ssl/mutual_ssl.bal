import ballerina/http;
import ballerina/log;

// Create an HTTP listener configuration, which will configure a listener to
// accept new connections that are secured via mutual SSL.
http:ServiceEndpointConfiguration helloWorldEPConfig = {
    secureSocket: {
        keyStore: {
            path: "${ballerina.home}/bre/security/ballerinaKeystore.p12",
            password: "ballerina"
        },
        trustStore: {
            path: "${ballerina.home}/bre/security/ballerinaTruststore.p12",
            password: "ballerina"
        },
         // Configure the preferred SSL protocol and the versions to enable.
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
listener http:Listener helloWorldEP = new(9095, config = helloWorldEPConfig);

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
        // Send response to the caller.
        var result = caller->respond("Successful");
        if (result is error) {
            log:printError("Error in responding", err = result);
        }
    }
}

// Create a new client configuration to be passed to the client endpoint.
// Configure the `keyStoreFile`, `keyStorePassword`, `trustStoreFile`, and
// The`trustStorePassword`, which is required to enable mutual SSL.
http:ClientEndpointConfig clientEPConfig = {
    secureSocket: {
        keyStore: {
            path: "${ballerina.home}/bre/security/ballerinaKeystore.p12",
            password: "ballerina"
        },
        trustStore: {
            path: "${ballerina.home}/bre/security/ballerinaTruststore.p12",
            password: "ballerina"
        },
        protocol: {
            name: "TLS"
        },
        ciphers: ["TLS_ECDHE_RSA_WITH_AES_128_CBC_SHA"]
    }
};

public function main() {
    // Create an HTTP client to interact with the created listener endpoint.
    http:Client clientEP = new("https://localhost:9095",
                                config = clientEPConfig);
    // Send a GET request to the listener.
    var resp = clientEP->get("/hello");
    if (resp is http:Response) {
        // If the request is successful, retrieve the text payload from the
        // response.
        var payload = resp.getTextPayload();
        if (payload is string) {
            // Log the retrieved text paylod.
            log:printInfo(payload);
        } else {
            // If an error occurs while retrieving the text payload, log
            // the error.
            log:printError(<string>payload.detail().message);
        }
    } else {
        // If an error occurs when getting the response, log the error.
        log:printError(<string>resp.detail().message);
    }
}
